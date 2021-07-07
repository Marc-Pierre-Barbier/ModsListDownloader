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
	public String[] fileList = (new File("mods")).list();
	
	public SortedMap<Integer, String> installedMods;
	
	public File modsMap = new File("mods/modsMap.sav");
	
	public IntegrityChecker() {
		if (this.modsMap.exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.modsMap));
				this.installedMods = (SortedMap<Integer, String>)ois.readObject();
				if (this.installedMods == null)
				this.installedMods = new TreeMap<>(); 
				ois.close();
			} catch (Exception e) {
				this.installedMods = new TreeMap<>();
				this.modsMap.delete();
			} 
		} else {
			try {
				this.modsMap.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		} 
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.modsMap));
			oos.writeObject(this.installedMods);
			oos.close();
		} catch (IOException e) {
			Log.e("SAV", "erreur pas de sauvegarde");
			System.exit(1);
		} 
	}
	
	public boolean checkAndDelete(File modToDownload, int modid) {
		String alredyInstalledName = ((String)this.installedMods.get(Integer.valueOf(modid))).toLowerCase();
		File alredyInstalledMods = null;
		File dir = new File("mods/");
		byte b;
		int i;
		String[] arrayOfString;
		for (i = (arrayOfString = dir.list()).length, b = 0; b < i; ) {
			String fileName = arrayOfString[b];
			if (fileName.equalsIgnoreCase(alredyInstalledName))
			alredyInstalledMods = new File("mods/" + fileName); 
			b++;
		} 
		if (alredyInstalledMods == null) {
			Log.e("IntegrityChecker", "alredy installed mod not found (" + alredyInstalledName + ")");
			this.installedMods.remove(Integer.valueOf(modid));
			return false;
		} 
		if (alredyInstalledMods.exists()) {
			try {
				(new ZipFile(alredyInstalledMods)).close();
			} catch (IOException e) {
				Log.i("integrityChecker", "Corruption detected redownloading");
				alredyInstalledMods.delete();
				this.installedMods.remove(Integer.valueOf(modid));
				updateFile();
				return false;
			} 
			if (!modToDownload.getAbsolutePath().equalsIgnoreCase(alredyInstalledMods.getAbsolutePath())) {
				alredyInstalledMods.delete();
				Log.i("integrityChecker", "Outdated mod detected redownloading (" + alredyInstalledName + " -> " + modToDownload.getName() + ")");
				this.installedMods.remove(Integer.valueOf(modid));
				updateFile();
				return false;
			} 
			return true;
		} 
		if (modToDownload.exists()) {
			this.installedMods.put(Integer.valueOf(modid), modToDownload.getName());
			updateFile();
			Log.i("interessting", modToDownload.getName());
		} 
		return false;
	}
	
	public boolean checkAndDelete(File modToDownload, String slug) {
		if (modToDownload.exists())
		try {
			(new ZipFile(modToDownload)).close();
			return true;
		} catch (IOException e) {
			Log.i("integrityChecker", "Corruption detected redownloading");
			modToDownload.delete();
			return false;
		}  
		byte b;
		int i;
		String[] arrayOfString;
		for (i = (arrayOfString = this.fileList).length, b = 0; b < i; ) {
			String s = arrayOfString[b];
			if (s.toLowerCase().contains(slug.trim())) {
				if (modToDownload.getName().trim().equalsIgnoreCase(s.trim()))
				return true; 
				(new File(s)).delete();
				break;
			} 
			b++;
		} 
		return false;
	}
	
	public void addModsToTheList(String fileName, int modID) {
		this.installedMods.put(Integer.valueOf(modID), fileName);
		Main.changes.set(Main.changes.get() + 1);
	}
	
	public boolean isModIdKnown(int modId) {
		return (this.installedMods.get(Integer.valueOf(modId)) != null);
	}
	
	public void updateFile() {
		try {
			this.modsMap.delete();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.modsMap));
			oos.writeObject(this.installedMods);
			oos.close();
		} catch (IOException iOException) {}
	}
}
