package downloader;

import java.io.File;
import java.net.MalformedURLException;

public class NewLineInterpreter extends Thread {
    private String line;
    
    private IntegrityChecker updateManager;
    
    private Database db;
    
    public NewLineInterpreter(String line, IntegrityChecker updateManager, Database db) {
        this.line = line;
        this.updateManager = updateManager;
        this.db = db;
    }
    
    public void run() {
        super.run();
        interpretLine();
    }
    
    private void interpretLine() {
        if (this.line.startsWith("direct=")) {
            String url = this.line.replaceFirst("direct=.*@", "");
            String filename = extractNameFromLink(this.line).toLowerCase();
            String slug = extractSlugFromLink(this.line).toLowerCase();
            boolean upToDate = this.updateManager.checkAndDelete(new File("mods/" + filename), slug);
            if (!upToDate) {
                if (Main.isVersbose())
                Log.i("main", "downloading " + filename); 
                if (!Main.isCheckingOnly())
                try {
                    HttpHelper.readFileFromUrlToFolder(url, "mods", filename);
                } catch (MalformedURLException e) {
                    Log.e("main", "error bad mod url " + this.line);
                }  
            } else if (Main.isVersbose()) {
                Log.i("main", "alredy installed");
            } 
        } else if ((this.line.split("/")).length >= 5 && !this.db.fetchMod(this.line)) {
            Log.e("main", "error could not get " + this.line);
        } 
    }
    
    private String extractSlugFromLink(String line) {
        char[] chars = new char[line.length() - line.indexOf(";") - "direct=".length() + 1];
        line.getChars("direct=".length(), line.indexOf(";"), chars, 0);
        return new String(chars);
    }
    
    private String extractNameFromLink(String line) {
        char[] chars = new char[line.indexOf("@") - line.indexOf(";")];
        line.getChars(line.indexOf(";") + 1, line.indexOf("@"), chars, 0);
        return new String(chars);
    }
}
