package downloader.helper;

import java.io.File;

public class SlugHelper {
    
    public static void deleteBySlug(String slug) {
        File modFolder = new File("mods");
        File[] mods = modFolder.listFiles();
        for( File mod : mods) {
            if(mod.getName().toLowerCase().contains((slug.toLowerCase()))) {
                mod.delete();
            }
        }
    }
}
