package downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;

import downloader.helper.ArchiveHelper;
import downloader.helper.HttpHelper;

public class DatabaseVersionManager {

    private String version;
    private static final String Iam = "DBUpdater";

    public DatabaseVersionManager() {
        try {
            this.version = fetchVersionFromFile();
        } catch (FileNotFoundException e) {
            this.version = null;
        }
    }

    public void updateIfNeeded() {
        String latestVersion = fetchLatestVersion();

        if( !version.equals(latestVersion) ) {
            Log.i(Iam, "new dbVersion avaliable downloading...");
            downloadDatabase(latestVersion);
        } else {
            Log.i(Iam, "update to Date");
        }
    }

    private void downloadDatabase(String dbVersion) {
        Log.i(Iam, "fetching=" + "http://files.mcdex.net/data/mcdex-v5-" + dbVersion + ".dat.bz2");
        File archive;
        try {
            archive = HttpHelper.readFileFromUrl("http://files.mcdex.net/data/mcdex-v5-" + dbVersion + ".dat.bz2");
        } catch (MalformedURLException e) {
            Log.e(Iam, "Error invalid database download url");
            System.exit(1);
            //utile pour le lint
            return;
        }
        Log.i(Iam, "download finished");
        Log.i(Iam, "decompressing db");
        try {
            ArchiveHelper.decompressBz2(archive, "database.dat");
            archive.delete();
        } catch (IOException e) {
            Log.e(Iam, "[ERROR]cannot extract database");
            if(archive.exists()) archive.delete();
            System.exit(1);
        }
    }

    private String fetchLatestVersion() {
		try {
            return HttpHelper.readStringFromUrl("http://files.mcdex.net/data/latest.v5");
        } catch (MalformedURLException e) {
            //erreur critique il ne faut pas continuer
            e.printStackTrace();
            System.exit(1);
        }
        return null;
	}

    private static String fetchVersionFromFile() throws FileNotFoundException {
        File versionFile = new File("dbVersion");
        if( versionFile.exists()) {
            BufferedReader bfr = new BufferedReader(new FileReader(versionFile));
            try {
                String version = bfr.readLine();
                bfr.close();
                return version;
            } catch (IOException e) {
                //si on peut pas lire le fichier on le suprime
                versionFile.delete();
                return null;
            }
        } else {
            throw new FileNotFoundException();
        }
    }
}
