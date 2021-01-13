package downloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

public class HttpHelper {

	public static String[] fileList;

	public static String readStringFromUrl(String urlToFetch) throws MalformedURLException {
		URL url = new URL(urlToFetch);
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) url.openConnection();
		} catch (IOException e1) {
			e1.printStackTrace();
			return "error";
		}

		String str = "";

		try (InputStream in = con.getInputStream()) {
			// par tranche de 4mo
			for (;;) {
				byte[] buffer = new byte[4096];
				int nbBytesRead = in.read(buffer);
				if (nbBytesRead < 0)
					break;

				for (int i = 0; i < nbBytesRead; i++) {
					str += (char) buffer[i];
				}
			}

		} catch (IOException e) {
		}

		return str;
	}

	public static File readFileFromUrl(String slug, String urlToFetch) throws MalformedURLException {
		String[] url = urlToFetch.split("/");
		if (url.length == 0)
			throw new MalformedURLException();

		return readFileFromUrl(slug, urlToFetch, url[url.length - 1]);
	}

	public static File readFileFromUrl(String slug, String urlToFetch, String filename) throws MalformedURLException {
		return readFileFromUrl(slug, urlToFetch, new File(filename));
	}

	static File readFileFromUrlToFolder(String slug, String urlToFetch, String folder) throws MalformedURLException {
		try {
			Files.createDirectories(Paths.get(folder));
		} catch (IOException e) {
			System.out.println("could not create folder");
			return null;
		}

		if (!folder.endsWith("/"))
			folder += "/";

		String[] url = urlToFetch.split("/");
		if (url.length == 0)
			throw new MalformedURLException();
		String name = url[url.length - 1];

		return readFileFromUrl(slug, urlToFetch, new File(folder + name));
	}

	private static File readFileFromUrl(String slug, String urlToFetch, File destination) throws MalformedURLException {
		URL url = new URL(urlToFetch);
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) url.openConnection();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		File downloadingFile = destination;

		if (slug != null)
			checkAndDelete(downloadingFile, slug);

		if (downloadingFile.exists()) {
			return downloadingFile;
		} else {
			try {
				downloadingFile.createNewFile();
			} catch (IOException e) {
				System.out.println("permission error could not write file");
				System.exit(1);
			}
		}
		FileOutputStream downloadingWriter;
		try {
			downloadingWriter = new FileOutputStream(downloadingFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return null;
			// imposible vu qu'on l'a crée precedament
		}

		try (InputStream in = con.getInputStream()) {
			int totalRead = 0;
			// on lit par tranche de 4mo
			byte[] buffer = new byte[4096];

			while (true) {
				int bytesRead = in.read(buffer);
				if (bytesRead < 0)
					break;
				totalRead += bytesRead;
				downloadingWriter.write(buffer, 0, bytesRead);
				System.out.println("\r" + totalRead);
			}
			downloadingWriter.close();
			return downloadingFile;
		} catch (IOException e) {
		}
		return null;

	}

	// suprime les versions obselettes
	private static void checkAndDelete(File fForgeSvcFile, String slug) {
		if (fileList == null) {
			fileList = new File("mods").list();
		}

		if (fForgeSvcFile.exists()) {
			try {
				//si sa balance une exception sa veut dire que le jar n'est pas lisible
				new ZipFile(fForgeSvcFile).close();
			} catch (IOException e) {
				fForgeSvcFile.delete();
				return;
			}
			
			for (String s : fileList) {
				if (s.contains(slug)) {
					if (!fForgeSvcFile.getName().contains(s)) {
						new File(s).delete();
					}
					return;
				}
			}

		}
	}
}
