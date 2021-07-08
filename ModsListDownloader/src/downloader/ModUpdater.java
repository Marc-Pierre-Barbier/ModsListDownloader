package downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;

import downloader.forgeSvc.ForgeSvcFile;
import downloader.helper.HttpHelper;

public class ModUpdater {
    private DirectUpdateManager directUpdateManager;
    private CurseUpdateManager curseUpdateManager;
    private Database db;
    
    public ModUpdater() {
        this.db = new Database();
        this.directUpdateManager = new DirectUpdateManager();
        this.curseUpdateManager = new CurseUpdateManager();
    }

    public void update() {
		File modsList = new File("modpack.txt");

        if(!(modsList.exists() && modsList.isFile())) {
            Log.e("Updater", "[ERROR]no mod file found exiting...");
            System.exit(1);
        }

        //parse and download
		try {
			//TODO: add threads
		    BufferedReader in = new BufferedReader(new FileReader(modsList));
		    while (in.ready()) {
		    	String line = in.readLine();
		    	if (!isAComment(line)) {
		    		if(line.startsWith("direct="))
		    		{
		    			handleDirectDownload(line);
		    		} else if (line.startsWith("del=")) {
                        String[] delete = line.split("del=");
                        new File(delete[1]).delete();
                    }
                    else {
		    			if (line.split("/").length >= 5) {
                            handleCurseDownload(line);
		    			}
		    		}
		    	}
		    }
            in.close();
            Log.i("Main", "finished");

        } catch( IOException e ) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void handleDirectDownload(String line) {
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

    public void handleCurseDownload(String line) {
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

    public static boolean isAComment(String line)
	{
		return line.startsWith("//");
	}

    private static String extractSlugFromLink(String line) {
		char[] chars = new char[line.length()-line.indexOf(";")-"direct=".length()+1];
		line.getChars("direct=".length(), line.indexOf(";"), chars, 0);
		return new String(chars);
	}
	
	private static String extractNameFromLink(String line) {
		char[] chars = new char[line.indexOf("@")-line.indexOf(";")];
		line.getChars(line.indexOf(";")+1, line.indexOf("@"), chars, 0);
		return new String(chars);
	}
}
