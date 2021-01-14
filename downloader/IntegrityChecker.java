package downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipFile;

public class IntegrityChecker {
	public String[] fileList;
	public SortedMap<Integer, String> installedMods;
	public File modsMap;
	
	@SuppressWarnings("unchecked")
	public IntegrityChecker()
	{
		fileList = new File("mods").list();
		modsMap = new File("mods/modsMap.sav");
		if(modsMap.exists())
		{
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modsMap));
				installedMods = (SortedMap<Integer, String>) ois.readObject();
				if(installedMods == null)installedMods = new TreeMap<>();
				ois.close();
			} catch (Exception e) {
				installedMods = new TreeMap<>();
				modsMap.delete();
			}
		}else {
			try {
				modsMap.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modsMap));
			oos.writeObject(installedMods);
			oos.close();
		} catch (IOException e) {
			Log.e("SAV", "erreur pas de sauvegarde");
			System.exit(1);
		}
	}
	
	public boolean checkAndDelete(File modToDownload, int modid) {
		File alredyInstalledMods = new File("mods/"+installedMods.get(modid));
		if (alredyInstalledMods.exists()) {
			try {
				//si sa balance une exception sa veut dire que le jar n'est pas lisible
				new ZipFile(alredyInstalledMods).close();
			} catch (IOException e) {
				Log.i("integrityChecker","Corruption detected redownloading");
				alredyInstalledMods.delete();
				return false;
			}
			if(!modToDownload.getAbsolutePath().equals(alredyInstalledMods.getAbsolutePath()))
			{
				alredyInstalledMods.delete();
				Log.i("integrityChecker","Corruption detected redownloading");
				return false;
			}
			return true;
		}else {
			if(modToDownload.exists())
			{
				modToDownload.delete();
			}
		}
		return false;
	}
	
	
	
	public boolean checkAndDelete(File modToDownload, String slug) {
		if (modToDownload.exists()) {
			try {
				//si sa balance une exception sa veut dire que le jar n'est pas lisible
				new ZipFile(modToDownload).close();
			} catch (IOException e) {
				Log.i("integrityChecker","Corruption detected redownloading");
				modToDownload.delete();
				return false;
			}
			
			for (String s : fileList) {
				if (s.contains(slug)) {
					if (!modToDownload.getName().contains(s)) {
						new File(s).delete();
						Log.i("integrityChecker","Corruption detected redownloading");
						return false;
					}
					return true;
				}
			}
			return false;

		}
		return false;
	}
	
	public void addModsToTheList(String fileName,int modID)
	{
		installedMods.put(modID, fileName);	
		updateFile();
	}
	
	public boolean isModIdKnown(int modId)
	{
		return installedMods.get(modId) != null;
	}
	
	private void updateFile()
	{
		try {
			modsMap.delete();
			//modsMap.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modsMap));
			oos.writeObject(installedMods);
			oos.close();
		} catch (IOException e) {}
	}

}
