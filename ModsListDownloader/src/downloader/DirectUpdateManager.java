package downloader;

import java.io.File;

import downloader.helper.ArchiveHelper;
import downloader.helper.SlugHelper;

public class DirectUpdateManager {
    
    /**
     * @param filename
     * @param slug
     * @return upToDate
     */
    public boolean checkAndDelete(String filename, String slug) {
        File mod = new File("mods/"+filename.toLowerCase());
        
        if (!mod.exists()) {
            SlugHelper.deleteBySlug(slug);
            return false;
        } else {
            return ArchiveHelper.checkJarIntegrity(mod);
        }

    }
}
