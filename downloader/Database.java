package downloader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
	IntegrityChecker integrityChecker;
	File dbVer;

	public Database() {
		dbVer = new File("dbVersion");
		if (dbVer.exists()) {
			try {
				BufferedReader bfr = new BufferedReader(new FileReader(dbVer));
				this.version = bfr.readLine();
				bfr.close();

				connector = new DBConnector();
				connector.connect(new File("database.dat").getAbsolutePath());

			} catch (IOException | SQLException e) {
				dbVer.delete();
				version = "";
			}
		} else {
			version = "";
		}
		gson = new Gson();
		integrityChecker = new IntegrityChecker();

	}

	public void updateDatabase() {
		try {
			String dbVersion = HttpHelper.readStringFromUrl("http://files.mcdex.net/data/latest.v5");
			if (dbVersion.equals(version)) {
				Log.i("DB", "alredy up to date");
				return;
			}
			if (dbVersion.equals("error")) {
				Log.i("DB", "an error ocured");
				return;
			}

			Log.i("DB", "newer version found");
			if (!this.version.isEmpty()) {
				Log.i("DB", "current=" + this.version + " remote=" + dbVersion);
			} else {
				Log.i("DB", "new=" + dbVersion);
			}

			Log.i("DB", "fetching=" + "http://files.mcdex.net/data/mcdex-v5-" + dbVersion + ".dat.bz2");
			File archive = HttpHelper.readFileFromUrl("http://files.mcdex.net/data/mcdex-v5-" + dbVersion + ".dat.bz2");
			Log.i("DB", "download finished");
			Log.i("DB", "decompressing db");
			File decompressedDatabase;
			try {
				decompressedDatabase = decompressBz2(archive, "database.dat");
			} catch (IOException e) {
				Log.i("DB", "cannot extract database");
				return;
			}
			Log.i("DB", "done");
			Log.i("DB", "loading the database");
			connector = new DBConnector();
			try {
				connector.connect(decompressedDatabase.getAbsolutePath());
			} catch (SQLException e) {
				return;
			}
			Log.i("DB", "database loaded");
			this.version = dbVersion;
			Log.i("DB", "saving db data");
			dbVer.delete();
			try {
				dbVer.createNewFile();
				PrintWriter p = new PrintWriter(dbVer);
				p.print(version);
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
				String downloadUrl = file.getDownloadUrl(modID);

				Log.i("MOD", "checking " + pj.getName().replace(" ", "-"));
				// si on connais le mod alors on se base si son fichier sinon on essay de le
				// trouver
				boolean upToDate;
				if (integrityChecker.isModIdKnown(modID)) {
					upToDate = integrityChecker
							.checkAndDelete(new File("mods/" + HttpHelper.getFileNameFromURL(downloadUrl)), modID);
				} else {
					upToDate = integrityChecker.checkAndDelete(
							new File("mods/" + HttpHelper.getFileNameFromURL(downloadUrl)), pj.getSlug());
				}
				if (!upToDate) {
					// on remplace les espace par des tiret pour eviter toute confusion
					Log.i("MOD", "downloading  " + pj.getName().replace(" ", "-"));
					HttpHelper.readFileFromUrlToFolder(downloadUrl, "mods");
					Log.i("MOD", "download finished");
					if (!integrityChecker.isModIdKnown(modID)) {
						integrityChecker.addModsToTheList(HttpHelper.getFileNameFromURL(downloadUrl), modID);
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
		String projectUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/" + mod.getProjectID();
		String jsonProject;
		try {
			jsonProject = HttpHelper.readStringFromUrl(projectUrl);
			if (jsonProject.equals("error"))
				return null;
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
