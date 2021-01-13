package downloader;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class IntegrityChecker {
	public static String[] fileList;
	
	public IntegrityChecker()
	{
		fileList = new File("mods").list();
	}
	
	public void checkAndDelete(File modToDownload, String slug) {
		if (modToDownload.exists()) {
			try {
				//si sa balance une exception sa veut dire que le jar n'est pas lisible
				new ZipFile(modToDownload).close();
			} catch (IOException e) {
				Log.i("integrityChecker","Corruption detected redownloading");
				modToDownload.delete();
				return;
			}
			
			for (String s : fileList) {
				if (s.contains(slug)) {
					if (!modToDownload.getName().contains(s)) {
						new File(s).delete();
						Log.i("integrityChecker","Corruption detected redownloading");
					}
					return;
				}
			}

		}
	}

}
