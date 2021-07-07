package downloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class HttpHelper {
	public static String readStringFromUrl(String urlToFetch) throws MalformedURLException {
		HttpURLConnection con;
		URL url = new URL(urlToFetch);
		try {
			con = (HttpURLConnection)url.openConnection();
		} catch (IOException e1) {
			e1.printStackTrace();
			return "error";
		} 
		String str = "";
		try {
			Exception exception2, exception1 = null;
		} catch (IOException iOException) {}
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
		if (name == null)
		throw new MalformedURLException(); 
		return readFileFromUrlToFolder(urlToFetch, folder, name);
	}
	
	public static File readFileFromUrlToFolder(String urlToFetch, String folder, String name) throws MalformedURLException {
		try {
			Files.createDirectories(Paths.get(folder, new String[0]), (FileAttribute<?>[])new FileAttribute[0]);
		} catch (IOException e) {
			Log.e("HTTPHelper", "could not create folder");
			return null;
		} 
		if (!folder.endsWith("/"))
		folder = String.valueOf(folder) + "/"; 
		return readFileFromUrl(urlToFetch, new File((String.valueOf(folder) + name.toLowerCase()).trim()));
	}
	
	private static File readFileFromUrl(String urlToFetch, File destination) throws MalformedURLException {
		HttpURLConnection con;
		FileOutputStream downloadingWriter;
		URL url = new URL(urlToFetch);
		try {
			con = (HttpURLConnection)url.openConnection();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		} 
		File downloadingFile = destination;
		System.out.println(downloadingFile.getAbsolutePath());
		if (downloadingFile.exists())
		return downloadingFile; 
		try {
			System.out.println(downloadingFile);
			downloadingFile.createNewFile();
		} catch (IOException e) {
			Log.e("HTTPHelper", "permission error could not write file");
			e.printStackTrace();
			System.exit(1);
		} 
		try {
			downloadingWriter = new FileOutputStream(downloadingFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return null;
		} 
		try {
			Exception exception1 = null, exception2 = null;
			try {
				
			} finally {
				exception2 = null;
				if (exception1 == null) {
					exception1 = exception2;
				} else if (exception1 != exception2) {
					exception1.addSuppressed(exception2);
				} 
			} 
		} catch (IOException iOException) {
			return null;
		} 
	}
	
	public static String getFileNameFromURL(String url) {
		String[] urlsplit = url.split("/");
		if (urlsplit.length == 0)
		return null; 
		return urlsplit[urlsplit.length - 1];
	}
}
