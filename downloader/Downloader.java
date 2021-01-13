package downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

public class Downloader {
	public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {
		System.out.println("hello");
		
		Database db = new Database();
		db.updateDatabase();

		File f = new File("modpack.txt");
		BufferedReader in = new BufferedReader(new FileReader(f));
		while (in.ready()) {
			String line = in.readLine();
			if (!line.startsWith("//")) {
				if (line.split("/").length >= 5 && ! line.startsWith("direct=")) {
					if (!db.fetchMod(line)) {
						System.err.println("error could not get " + line);
					}
				}else {
					if(line.startsWith("direct@"))
					{
						char[] chars = new char[line.length()-line.indexOf("@")-"direct=".length()+1];
						line.getChars("direct=".length(), line.indexOf("@"), chars, 0);
						String slug = new String(chars);
						
						String[] url = line.replace("direct=*@", "").split("/");
						System.out.println("downloading "+ url[url.length -1 ]);
						HttpHelper.readFileFromUrlToFolder(slug,line.replace("direct@", ""),"mods");
						System.out.println("done");
					}
				}
			}
		}
		in.close();
	}

}
