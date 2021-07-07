package downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;

import downloader.forgeSvc.ForgeSvcEntry;
import downloader.forgeSvc.ForgeSvcFile;
import downloader.helper.HttpHelper;

public class Database {
	DBConnector connector;
	IntegrityChecker integrityChecker;

	Gson gson;
			
	public Database() {
		this.connector = new DBConnector();
		try {
			this.connector.connect((new File("database.dat")).getAbsolutePath());
		} catch (SQLException e) {
			//database hs
			System.exit(1);
		}

		integrityChecker = new IntegrityChecker();
		this.gson = new Gson();
	}
	
	private int findProjectBySlug(String slug, int ptype) {
		int modID = -1;
		ResultSet rs = this.connector.executeRequest("select projectid from projects where type =" + ptype + " and slug =\"" + 
		slug.trim().toLowerCase() + "\"");
		try {
			if (rs.next()) {
				modID = rs.getInt("projectid");
			} else {
				Log.e("DB", "not found " + slug);
				return -1;
			} 
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		} 
		return modID;
	}
	
	private int findModBySlug(String slug) {
		return findProjectBySlug(slug, 0);
	}
	
	private ProjectInfo getProjectInfo(int projectId) {
		ResultSet rs = this.connector.executeRequest(
		"select slug, name, description from projects where projectid = " + projectId + " and type = 0");
		try {
			if (rs.next())
				return new ProjectInfo(rs.getString("slug"), rs.getString("name"), rs.getString("description")); 
		} catch (SQLException sQLException) {}
		return null;
	}
	
	public boolean fetchMod(String string) {
		String name = string.split("/")[5];
		int modID = findModBySlug(name);
		if (modID == -1) {
			Log.e("DB", "could not find mod id");
			return false;
		} 
		ProjectInfo pj = getProjectInfo(modID);
		if (pj == null)
			return false; 
		CurseForgeModFile modfile = new CurseForgeModFile(pj, modID, -1, false);
		ForgeSvcFile file = getLastestFile("1.12.2", modfile);
		if (file != null) {
			try {
				boolean upToDate;
				String downloadUrl = file.getDownloadUrl(modID);
				if (Main.verbose)
					Log.i("MOD", "checking " + pj.getName().replace(" ", "-")); 
				if (this.integrityChecker.isModIdKnown(modID)) {
					if (Main.verbose)
						Log.i("DB", "found " + name); 
					upToDate = this.integrityChecker
					.checkAndDelete(new File("mods/" + HttpHelper.getFileNameFromURL(downloadUrl)), modID);
				} else {
					Log.i("DB", "NEW MOD DETECTED " + modID);
					upToDate = this.integrityChecker.checkAndDelete(
					new File("mods/" + HttpHelper.getFileNameFromURL(downloadUrl)), pj.getSlug());
				} 
				if (!upToDate) {
					if (Main.verbose)
						Log.i("MOD", "downloading  " + pj.getName().replace(" ", "-")); 
					if (!Main.checkingonly)
						HttpHelper.readFileFromUrlToFolder(downloadUrl, "mods"); 
					if (Main.verbose)
						Log.i("MOD", "download finished"); 
					if (!this.integrityChecker.isModIdKnown(modID) && !Main.checkingonly) {
						this.integrityChecker.addModsToTheList(HttpHelper.getFileNameFromURL(downloadUrl), modID);
						if (Main.verbose)
							Log.i("DB", "added a mod to the registry"); 
					} 
				} 
				return true;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} 
		} else {
			Log.e("DB", "no file in server");
		} 
		return false;
	}
	
	public ForgeSvcFile getLastestFile(String minecraftVer, CurseForgeModFile mod) {
		String jsonProject, projectUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/" + mod.getProjectID();
		try {
			jsonProject = HttpHelper.readStringFromUrl(projectUrl);
			if (jsonProject.equals("error"))
				return null; 
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} 
		ForgeSvcEntry entry = (ForgeSvcEntry)this.gson.fromJson(jsonProject, ForgeSvcEntry.class);
		byte b;
		int i;
		ForgeSvcFile[] arrayOfForgeSvcFile;
		for (i = (arrayOfForgeSvcFile = entry.getFiles()).length, b = 0; b < i; ) {
			ForgeSvcFile f = arrayOfForgeSvcFile[b];
			if (f.getGameVersion().equals(minecraftVer))
				return f; 
			b++;
		} 
		return null;
	}
}
