package downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import downloader.forgeSvc.ForgeSvcFile;
import downloader.helper.ArchiveHelper;
import downloader.helper.HttpHelper;

public class ModUpdater {
	private DirectUpdateManager directUpdateManager;
	private CurseUpdateManager curseUpdateManager;
	private Database db;
	private final String ME = "ModUpdater";

	public ModUpdater() {
		this.db = new Database();
		this.directUpdateManager = new DirectUpdateManager();
		this.curseUpdateManager = new CurseUpdateManager();
	}

	public void update() {
		File modsList = new File("modpack.txt");

        if(!(modsList.exists() && modsList.isFile())) {
            Log.e(ME, "[ERROR]no mod file found exiting...");
            System.exit(1);
        }

        //parse and download
		List<Thread> threads = new ArrayList<>(Main.threadNb);
		try {
			BufferedReader in = new BufferedReader(new FileReader(modsList));
			while (in.ready()) {
				String line = in.readLine();
				if (!isAComment(line)) {
					Thread t = new UpdaterThread(line, db, directUpdateManager, curseUpdateManager, threads);
					t.start();
					threads.add(t);
				}

				while(Main.threadNb == threads.size()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
			}
			in.close();
		} catch( IOException e ) {
			e.printStackTrace();
			System.exit(1);
		}

		//on attend la fin des threads
		while( !threads.isEmpty() ) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}

		if(! UpdaterThread.failedLines.isEmpty()) {
			List<String> failures = new ArrayList<>(UpdaterThread.failedLines);
			UpdaterThread.failedLines.clear();

			Log.e(ME, "filed to download " + failures.size());
			Log.e(ME, "Retrying but slower");

			for(String line : failures) {
				Thread t = new UpdaterThread(line, db, directUpdateManager, curseUpdateManager, threads);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {}
			}

			if(! UpdaterThread.failedLines.isEmpty()) {
				Log.e(ME, "could not download the following mods : ");
				for(String s: failures) {
					Log.e(ME, "    " + s);
				}
			}
		}


		Log.i(ME, "finished");
		curseUpdateManager.updateFile();
	}

	public static boolean isAComment(String line)
	{
		return line.startsWith("//");
	}
}


final class UpdaterThread extends Thread {
	private final String line;
	private final Database db;
	private final DirectUpdateManager directUpdateManager;
	private final CurseUpdateManager curseUpdateManager;
	private final List<Thread> threads;
	private static final String ME = "ModUpdaterThread";
	public static final List<String> failedLines = new ArrayList<>();

	public UpdaterThread(String line, Database db, DirectUpdateManager directUpdateManager, CurseUpdateManager curseUpdateManager, List<Thread> threads) {
		super(line);
		this.line = line;
		this.db = db;
		this.directUpdateManager = directUpdateManager;
		this.curseUpdateManager = curseUpdateManager;
		this.threads = threads;
	}

	@Override
	public void run() {
		try {
				//used for post download checks
			File downloadedFile = null;
			if(line.startsWith("direct=") && line.contains(";")  && line.contains("@"))
			{
				downloadedFile = handleDirectDownload(line);
			} else if (line.startsWith("del=")) {
				String[] delete = line.split("del=");
				if(new File("mods/" + delete[1]).delete()) {
					Log.i(ME, "Successfully removed " + delete[1]);
				}
			}
			else {
				if (line.split("/").length >= 5 && line.contains("curseforge")) {
					downloadedFile = handleCurseDownload(line);
				} else {
					Log.e(ME, "unrecognized line: " + line);
				}
			}

			if(downloadedFile != null && !ArchiveHelper.checkJarIntegrity(downloadedFile)) {
				downloadedFile.delete();
				failedLines.add(line);
			}
		} catch(Exception e) { e.printStackTrace(); }
		

		super.run();
		//on se retire de la pool d'execution
		this.threads.remove(this);
	}

	private File handleDirectDownload(String line) {
		String url = line.replaceFirst("direct=.*@", "");
		String filename=extractNameFromLink(line);
		
		//le slug est une nom de mods qui se trouve dans le nom du fichier
		String slug = extractSlugFromLink(line);
		//on check si le mods est pas déja installé et si il est a jours
		boolean upToDate = directUpdateManager.checkAndDelete(filename, slug);
		if(!upToDate)
		{
			Log.i(ME,"downloading "+ filename);
			try {
				Log.i(ME,"done");
				return HttpHelper.readFileFromUrlToFolder(url,"mods",filename);
			} catch (MalformedURLException e) {
				Log.e(ME, "could not update mod, error while downloading");
			}
		}
		return null;
	}

	private File handleCurseDownload(String line) {
		String name = line.split("/")[5];
		int modID = db.findModBySlug(name);
		ProjectInfo pj = db.getProjectInfo(modID);
		ForgeSvcFile svc = db.fetchMod(pj, modID);

        if (svc != null) {
			try {
				boolean upToDate;
				String downloadUrl = svc.getDownloadUrl(modID);
				
				Log.i("MOD", "checking " + pj.getName().replace(" ", "-"));
				if (this.curseUpdateManager.isModIdKnown(modID)) {
					Log.i("DB", "found " + name); 
					upToDate = this.curseUpdateManager.checkAndDelete(new File("mods/" + HttpHelper.getFileNameFromURL(downloadUrl)), modID);
				} 
				else {
					Log.i("DB", "NEW MOD DETECTED " + modID);
					upToDate = this.curseUpdateManager.checkAndDelete(new File("mods/" + HttpHelper.getFileNameFromURL(downloadUrl)), pj.getSlug());
					if(upToDate) {
						Log.i("who", "file found");
						this.curseUpdateManager.addModsToTheList("mods/" + HttpHelper.getFileNameFromURL(downloadUrl), modID);
					}
				} 

				if (!upToDate) {
					Log.i("MOD", "downloading  " + pj.getName().replace(" ", "-")); 
					File downloaded = HttpHelper.readFileFromUrlToFolder(downloadUrl, "mods"); 
					
					Log.i("MOD", "download finished");
					if (!this.curseUpdateManager.isModIdKnown(modID)) {
						this.curseUpdateManager.addModsToTheList(HttpHelper.getFileNameFromURL(downloadUrl), modID);
						Log.i("DB", "added a mod to the registry"); 
					} 
					return downloaded;
				} 
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} 
		} else {
			Log.e("DB", "no file in server");
		} 
		return null;
    }

	private static String extractNameFromLink(String line) {
		char[] chars = new char[line.indexOf("@")-line.indexOf(";")];
		line.getChars(line.indexOf(";")+1, line.indexOf("@"), chars, 0);
		return new String(chars);
	}

	private static String extractSlugFromLink(String line) {
		char[] chars = new char[line.length()-line.indexOf(";")-"direct=".length()+1];
		line.getChars("direct=".length(), line.indexOf(";"), chars, 0);
		return new String(chars);
	}
}
