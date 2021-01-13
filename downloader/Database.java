package downloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import com.google.gson.Gson;

import downloader.forgeSvc.ForgeSvcEntry;
import downloader.forgeSvc.ForgeSvcFile;

public class Database {

	String version;
	DBConnector connector;
	Gson gson;

	public Database() {

		// Todo save ver
		this.version = "";
		gson = new Gson();
		
	}

	public void updateDatabase() {
		try {
			String dbVersion = HttpHelper.readStringFromUrl("http://files.mcdex.net/data/latest.v5");
			if (dbVersion.equals(version)) {
				println("alredy up to date");
				return;
			}
			if (dbVersion.equals("error")) {
				println("an error ocured");
				return;
			}

			println("newer version found");
			if (!this.version.isEmpty()) {
				println("current=" + this.version + " remote=" + dbVersion);
			} else {
				println("new=" + dbVersion);
			}

			println("fetching=" + "http://files.mcdex.net/data/mcdex-v5-" + dbVersion + ".dat.bz2");
			File archive = HttpHelper.readFileFromUrl(null,"http://files.mcdex.net/data/mcdex-v5-" + dbVersion + ".dat.bz2");
			println("download finished");
			println("decompressing db");
			File decompressedDatabase;
			try {
				decompressedDatabase = decompressBz2(archive, "database.dat");
			} catch (IOException e) {
				println("cannot extract database");
				return;
			}
			println("done");
			println("loading the database");
			connector = new DBConnector();
			try {
				connector.connect(decompressedDatabase.getAbsolutePath());
			} catch (SQLException e) {
				return;
			}
			println("database loaded");
			this.version = dbVersion;

		} catch (MalformedURLException e) {
			println("database link is dead");
			System.exit(1);
		}
	}

	private void print(String message) {
		System.out.print("db : " + message);
	}

	private void println(String message) {
		print(message + "\n");
	}

	private File decompressBz2(File inputFile, String outputFile) throws IOException {
		BZip2CompressorInputStream input = new BZip2CompressorInputStream(
				new BufferedInputStream(new FileInputStream(inputFile)));
		File decompressedFile = new File(outputFile);
		FileOutputStream output = new FileOutputStream(decompressedFile);
		IOUtils.copy(input, output);
		return decompressedFile;
	}

	private int findProjectBySlug(String slug, int ptype) {
		int modID = -1;
		ResultSet rs = connector.executeRequest("select projectid from projects where type =" + ptype + " and slug =\""
				+ slug.trim().toLowerCase() + "\"");
		try {
			if (rs.next()) {
				modID = rs.getInt("projectid");
				System.out.println("modid:" + modID);
			} else {
				System.out.println("not found " + slug);
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
		System.out.println(
				"select slug, name, description from projects where projectid = " + projectId + " and type = 0");
		ResultSet rs = connector.executeRequest(
				"select slug, name, description from projects where projectid = " + projectId + " and type = 0");
		try {
			if (rs.next()) {
				return new ProjectInfo(rs.getString("slug"), rs.getString("name"), rs.getString("description"));
			}
		} catch (SQLException e) {
		}
		return null;
	}

	public boolean fetchMod(String string) {
		String name = string.split("/")[5];
		int modID = findModBySlug(name);
		if (modID == -1)
			return false;
		ProjectInfo pj = getProjectInfo(modID);
		if (pj == null)
			return false;
		CurseForgeModFile modfile = new CurseForgeModFile(pj, modID, -1, false);
		ForgeSvcFile file = getLastestFile("1.12.2", modfile);
		if (file != null) {
			try {
				print("downloading  " + pj.getName());
				HttpHelper.readFileFromUrlToFolder(pj.getSlug(),file.getDownloadUrl(modID), "mods");
				println("download finished");
				return true;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("no file in server");
		}
		return false;
	}

	public ForgeSvcFile getLastestFile(String minecraftVer, CurseForgeModFile mod) {
		String projectUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/" + mod.getProjectID();
		String jsonProject;
		try {
			jsonProject = HttpHelper.readStringFromUrl(projectUrl);
			if(jsonProject.equals("error"))return null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	
		ForgeSvcEntry entry = gson.fromJson(jsonProject, ForgeSvcEntry.class);

		for (ForgeSvcFile f : entry.getFiles()) {
				if (f.getGameVersion().equals(minecraftVer)) {
					return f;
			}
		}
		return null;
	}
	
	
	

}
