package downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
	public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {		
		
		IntegrityChecker updateManager = new IntegrityChecker();
		Database db = new Database();
		db.updateDatabase();

		File f;
		if(args.length == 0)f = new File("modpack.txt");
		else f = new File(args[0]);
		
		BufferedReader in = new BufferedReader(new FileReader(f));
		while (in.ready()) {
			String line = in.readLine();
			if (!isAComment(line)) {
				if(line.startsWith("direct="))
				{
					String filename = line.replaceFirst("direct=*@", "");
					
					//le slug est une nom de mods qui se trouve dans le nom du fichier
					String slug = extractSlugFromLink(line);
					//on check si le mods est pas déja installé et si il est a jours
					updateManager.checkAndDelete(new File("mods/"+filename), slug);
					
					String[] url = line.replaceFirst("direct=*@", "").split("/");
					Log.i("main","downloading "+ url[url.length -1 ]);
					
					HttpHelper.readFileFromUrlToFolder(filename,"mods");
					Log.i("main","done");
				}else {
					if (line.split("/").length >= 5 && !db.fetchMod(line)) {
							Log.e("main","error could not get " + line);
					}
				}
			}
		}
		in.close();
	}
	
	private static String extractSlugFromLink(String line) {
		char[] chars = new char[line.length()-line.indexOf("@")-"direct=".length()+1];
		line.getChars("direct=".length(), line.indexOf("@"), chars, 0);
		return new String(chars);
	}

	public static boolean isAComment(String line)
	{
		return line.startsWith("//");
	}

}
