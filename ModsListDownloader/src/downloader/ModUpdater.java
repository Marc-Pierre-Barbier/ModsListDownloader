package downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import downloader.forgeSvc.ForgeSvcFile;
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
            Log.i(ME, "finished");

        } catch( IOException e ) {
            e.printStackTrace();
            System.exit(1);
        }
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

	public UpdaterThread(String line, Database db, DirectUpdateManager directUpdateManager, CurseUpdateManager curseUpdateManager, List<Thread> threads) {
		super();
		this.line = line;
		this.db = db;
		this.directUpdateManager = directUpdateManager;
		this.curseUpdateManager = curseUpdateManager;
		this.threads = threads;
	}

	@Override
	public void run() {
		if(line.startsWith("direct="))
		{
			handleDirectDownload(line);
		} else if (line.startsWith("del=")) {
			String[] delete = line.split("del=");
			if(new File("mods/" + delete[1]).delete()) {
				Log.i(ME, "Successfully removed " + delete[1]);
			}
		}
		else {
			if (line.split("/").length >= 5) {
				handleCurseDownload(line);
			}
		}
		super.run();
		//on se retire de la pool d'execution
		this.threads.remove(this);
	}

	private void handleDirectDownload(String line) {
        String url = line.replaceFirst("direct=.*@", "");
		String filename=extractNameFromLink(line);
		
		//le slug est une nom de mods qui se trouve dans le nom du fichier
		String slug = extractSlugFromLink(line);
		//on check si le mods est pas déja installé et si il est a jours
		boolean upToDate = directUpdateManager.checkAndDelete(filename, slug);
		if(!upToDate)
		{
			Log.i("main","downloading "+ filename);
			try {
                HttpHelper.readFileFromUrlToFolder(url,"mods",filename);
                Log.i("main","done");
            } catch (MalformedURLException e) {
                Log.e("ModUpdater", "could not update mod, error while downloading");
            }
		}
    }

    private void handleCurseDownload(String line) {
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
				} 

				if (!upToDate) {
					Log.i("MOD", "downloading  " + pj.getName().replace(" ", "-")); 
					
					HttpHelper.readFileFromUrlToFolder(downloadUrl, "mods"); 
					
					Log.i("MOD", "download finished");
					if (!this.curseUpdateManager.isModIdKnown(modID)) {
						this.curseUpdateManager.addModsToTheList(HttpHelper.getFileNameFromURL(downloadUrl), modID);
						Log.i("DB", "added a mod to the registry"); 
					} 
				} 
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} 
		} else {
			Log.e("DB", "no file in server");
		} 
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
