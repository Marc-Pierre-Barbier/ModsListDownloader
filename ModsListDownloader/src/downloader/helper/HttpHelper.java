package downloader.helper;

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

import downloader.Log;

public class HttpHelper {
	private HttpHelper() {}

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

		} catch (IOException e) {}

		return str;
	}

	public static File readFileFromUrl(String urlToFetch) throws MalformedURLException {
		String[] url = urlToFetch.split("/");
		if (url.length == 0)
			throw new MalformedURLException();

		return readFileFromUrl(urlToFetch, url[url.length - 1]);
	}

	public static File readFileFromUrl(String urlToFetch, String filename) throws MalformedURLException {
		return readFileFromUrl(urlToFetch, new File(filename));
	}

	public static File readFileFromUrlToFolder(String urlToFetch, String folder) throws MalformedURLException {
		String name = getFileNameFromURL(urlToFetch);
		if(name == null)throw new MalformedURLException();

		return readFileFromUrlToFolder(urlToFetch, folder,name);
	}
	
	public static File readFileFromUrlToFolder(String urlToFetch, String folder,String name) throws MalformedURLException {
		try {
			Files.createDirectories(Paths.get(folder));
		} catch (IOException e) {
			Log.e("HTTPHelper","could not create folder");
			return null;
		}

		if (!folder.endsWith("/"))
			folder += "/";

		return readFileFromUrl(urlToFetch, new File((folder + name.toLowerCase()).trim()));
	}

	//TODO: debug
	private static File readFileFromUrl(String urlToFetch, File destination) throws MalformedURLException {
		URL url = new URL(urlToFetch);
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) url.openConnection();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		File downloadingFile = destination;
		System.out.println(downloadingFile.getAbsolutePath());

		if (downloadingFile.exists()) {
			return downloadingFile;
		} else {
			try {
				System.out.println(downloadingFile);
				downloadingFile.createNewFile();
			} catch (IOException e) {
				Log.e("HTTPHelper","permission error could not write file");
				e.printStackTrace();
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
			// on lit par tranche de 4mo
			byte[] buffer = new byte[4096];

			while (true) {
				int bytesRead = in.read(buffer);
				if (bytesRead < 0)
					break;
				downloadingWriter.write(buffer, 0, 	bytesRead);
			}
			downloadingWriter.close();
			return downloadingFile;
		} catch (IOException e) {}
		return null;

	}
	
	public static String getFileNameFromURL(String url)
	{
		String[] urlsplit = url.split("/");
		if (urlsplit.length == 0)
			return null;
		return urlsplit[urlsplit.length - 1];
	}

	
}
