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
					String url = line.replaceFirst("direct=.*@", "");
					String filename=extractNameFromLink(line);
					
					//le slug est une nom de mods qui se trouve dans le nom du fichier
					String slug = extractSlugFromLink(line);
					//on check si le mods est pas déja installé et si il est a jours
					boolean upToDate = updateManager.checkAndDelete(new File("mods/"+filename), slug);
					if(!upToDate)
					{
						Log.i("main","downloading "+ filename);
						HttpHelper.readFileFromUrlToFolder(url,"mods",filename);
						Log.i("main","done");
					}
				}else {
					if (line.split("/").length >= 5 && !db.fetchMod(line)) {
							Log.e("main","error could not get " + line);
					}
				}
			}
		}
		in.close();
		Log.i("Main", "finished");
	}
	
	private static String extractSlugFromLink(String line) {
		char[] chars = new char[line.length()-line.indexOf(";")-"direct=".length()+1];
		line.getChars("direct=".length(), line.indexOf(";"), chars, 0);
		return new String(chars);
	}
	
	private static String extractNameFromLink(String line) {
		char[] chars = new char[line.indexOf("@")-line.indexOf(";")];
		line.getChars(line.indexOf(";")+1, line.indexOf("@"), chars, 0);
		return new String(chars);
	}


	public static boolean isAComment(String line)
	{
		return line.startsWith("//");
	}

}
