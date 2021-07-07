package downloader;

import com.google.gson.Gson;
import downloader.forgeSvc.ForgeSvcEntry;
import downloader.forgeSvc.ForgeSvcFile;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class Database {
	String version;
	
	DBConnector connector;
	
	Gson gson;
	
	IntegrityChecker integrityChecker;
	
	File dbVer = new File("dbVersion");
	
	public Database() {
		if (this.dbVer.exists()) {
			try {
				BufferedReader bfr = new BufferedReader(new FileReader(this.dbVer));
				this.version = bfr.readLine();
				bfr.close();
				this.connector = new DBConnector();
				this.connector.connect((new File("database.dat")).getAbsolutePath());
			} catch (IOException|SQLException e) {
				this.dbVer.delete();
				this.version = "";
			} 
		} else {
			this.version = "";
		} 
		this.gson = new Gson();
		this.integrityChecker = new IntegrityChecker();
	}
	
	public void updateDatabase() {
		try {
			File decompressedDatabase;
			String dbVersion = HttpHelper.readStringFromUrl("http://files.mcdex.net/data/latest.v5");
			if (dbVersion.equals(this.version)) {
				Log.i("DB", "alredy up to date");
				return;
			} 
			if (dbVersion.equals("error") || dbVersion.trim().equals("")) {
				Log.i("DB", "an error ocured");
				System.exit(1);
			} 
			
			Log.i("DB", "newer version found");
			if (!this.version.isEmpty()) {
				Log.i("DB", "current=" + this.version + " remote=" + dbVersion);
			} else {
				Log.i("DB", "new=" + dbVersion);
			} 
			Log.i("DB", "fetching=http://files.mcdex.net/data/mcdex-v5-" + dbVersion + ".dat.bz2");
			File archive = HttpHelper.readFileFromUrl("http://files.mcdex.net/data/mcdex-v5-" + dbVersion + ".dat.bz2");
			Log.i("DB", "download finished");
			Log.i("DB", "decompressing db");
			try {
				decompressedDatabase = decompressBz2(archive, "database.dat");
			} catch (IOException e) {
				Log.i("DB", "cannot extract database");
				return;
			} 
			Log.i("DB", "done");
			Log.i("DB", "loading the database");
			this.connector = new DBConnector();
			try {
				this.connector.connect(decompressedDatabase.getAbsolutePath());
			} catch (SQLException e) {
				return;
			} 
			Log.i("DB", "database loaded");
			this.version = dbVersion;
			Log.i("DB", "saving db data");
			this.dbVer.delete();
			try {
				this.dbVer.createNewFile();
				PrintWriter p = new PrintWriter(this.dbVer);
				p.print(this.version);
				p.close();
			} catch (Exception e) {
				Log.e("DB", "failed to save db");
			} 
		} catch (MalformedURLException e) {
			Log.i("DB", "database link is dead");
			System.exit(1);
		} 
	}
	
	private File decompressBz2(File inputFile, String outputFile) throws IOException {
		BZip2CompressorInputStream input = new BZip2CompressorInputStream(
		new BufferedInputStream(new FileInputStream(inputFile)));
		File decompressedFile = new File(outputFile);
		FileOutputStream output = new FileOutputStream(decompressedFile);
		IOUtils.copy((InputStream)input, output);
		return decompressedFile;
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
				if (Main.isVersbose())
				Log.i("MOD", "checking " + pj.getName().replace(" ", "-")); 
				if (this.integrityChecker.isModIdKnown(modID)) {
					if (Main.isVersbose())
					Log.i("DB", "found " + name); 
					upToDate = this.integrityChecker
					.checkAndDelete(new File("mods/" + HttpHelper.getFileNameFromURL(downloadUrl)), modID);
				} else {
					Log.i("DB", "NEW MOD DETECTED " + modID);
					upToDate = this.integrityChecker.checkAndDelete(
					new File("mods/" + HttpHelper.getFileNameFromURL(downloadUrl)), pj.getSlug());
				} 
				if (!upToDate) {
					if (Main.isVersbose())
					Log.i("MOD", "downloading  " + pj.getName().replace(" ", "-")); 
					if (!Main.isCheckingOnly())
					HttpHelper.readFileFromUrlToFolder(downloadUrl, "mods"); 
					if (Main.isVersbose())
					Log.i("MOD", "download finished"); 
					if (!this.integrityChecker.isModIdKnown(modID) && !Main.isCheckingOnly()) {
						this.integrityChecker.addModsToTheList(HttpHelper.getFileNameFromURL(downloadUrl), modID);
						if (Main.isVersbose())
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
