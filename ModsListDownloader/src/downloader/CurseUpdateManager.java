package downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import downloader.helper.ArchiveHelper;
import downloader.helper.SlugHelper;

public class CurseUpdateManager {
	private SortedMap<Integer, String> installedMods;
	
	public CurseUpdateManager()
	{
		loadModMapFromFile();
	}
	
	@SuppressWarnings("unchecked")
	private void loadModMapFromFile() {
		File modsMapFile = new File("mods/modsMap.sav");
		if(modsMapFile.exists())
		{
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modsMapFile));
				installedMods = (SortedMap<Integer, String>) ois.readObject();
				ois.close();
			} catch (Exception e) {
				modsMapFile.delete();
			}
			if(installedMods == null)installedMods = new TreeMap<>();
		}
		this.updateFile();
	}

	public boolean checkAndDelete(File modToDownload, int modid) {
		String alredyInstalledName = installedMods.get(modid).toLowerCase();	
		File alredyInstalledMods= null;
		
		//on cherche le fichier local affin de ne pas voir d'erreur avec la casse
		File dir = new File("mods/");
		for(String fileName : dir.list())
		{
			if(fileName.equalsIgnoreCase(alredyInstalledName))
			{
				alredyInstalledMods = new File("mods/"+fileName);
				break;
			}
		}

		if(alredyInstalledMods == null) {
			Log.e("IntegrityChecker", "alredy installed mod not found ("+alredyInstalledName+")");
			installedMods.remove(modid);
			
			return false;
		}

		if (alredyInstalledMods.exists()) {
			//we only check if the jar file still works
			if(ArchiveHelper.checkJarIntegrity(alredyInstalledMods)) {
				alredyInstalledMods.delete();
				installedMods.remove(modid);
				updateFile();
				return false;
			}

			if(!modToDownload.getAbsolutePath().equalsIgnoreCase(alredyInstalledMods.getAbsolutePath()))
			{
				alredyInstalledMods.delete();
				Log.i("integrityChecker","Outdated mod detected redownloading ("+alredyInstalledName+" -> "+modToDownload.getName()+")");
				installedMods.remove(modid);
				updateFile();
				return false;
			}
			return true;
		}else {
			if(modToDownload.exists())
			{
				installedMods.put(modid, modToDownload.getName());
				updateFile();
				Log.i("CurseUpdater","existing file detected adding it to the register: " + modToDownload.getName());
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * @return true == file ok
	 */
	public boolean checkAndDelete(File modToDownload, String slug) {
		if (modToDownload.exists()) {
			if (ArchiveHelper.checkJarIntegrity(modToDownload)) {
				return true;
			} else {
				SlugHelper.deleteBySlug(slug);
				return false;
			}
		}
		return false;
	}
	
	/**
	 * un chemin d'acces ne marche pas
	 * @param fileName
	 * @param modID
	 */
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
		File modsMap = new File("mods/modsMap.sav");
		try {
			if(modsMap.exists())modsMap.delete();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modsMap));
			oos.writeObject(installedMods);
			oos.close();
		} catch (IOException e) {
			Log.e("SAV", "erreur pas de sauvegarde");
		}
	}

}
