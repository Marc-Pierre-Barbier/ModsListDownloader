package downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import com.google.gson.Gson;

import downloader.forgeSvc.ForgeSvcEntry;
import downloader.forgeSvc.ForgeSvcFile;
import downloader.helper.HttpHelper;

public class Database {
	DBConnector connector;

	Gson gson;
			
	public Database() {
		this.connector = new DBConnector();
		try {
			this.connector.connect((new File("database.dat")).getAbsolutePath());
		} catch (SQLException e) {
			//database hs
			System.exit(1);
		}
		this.gson = new Gson();
	}
	
	private int findProjectBySlug(String slug, int ptype) {
		int pjId = -1;
		ResultSet rs = this.connector.executeRequest("select projectid from projects where type =" + ptype + " and slug LIKE \"%" + slug.trim().toLowerCase() + "\"%");
		try {
			if (rs.next()) {
				pjId = rs.getInt("projectid");
			} else {
				Log.e("DB", "not found " + slug);
				return -1;
			} 
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		} 
		return pjId;
	}
	
	public int findModBySlug(String slug) {
		return findProjectBySlug(slug, 0);
	}
	
	public ProjectInfo getProjectInfo(int projectId) {
		ResultSet rs = this.connector.executeRequest(
		"select slug, name, description from projects where projectid = " + projectId + " and type = 0");
		try {
			if (rs.next())
				return new ProjectInfo(rs.getString("slug"), rs.getString("name"), rs.getString("description")); 
		} catch (SQLException sQLException) {}
		return null;
	}
	
	public ForgeSvcFile fetchMod(ProjectInfo pj, int modID) {
		CurseForgeModFile modfile = new CurseForgeModFile(pj, modID, -1, false);
		return getLastestFile(modfile);
	}
	
	public ForgeSvcFile getLastestFile(CurseForgeModFile mod) {
		String jsonProject;
		String projectUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/" + mod.getProjectID();
		try {
			jsonProject = HttpHelper.readStringFromUrl(projectUrl);
			if (jsonProject.equals("error"))
				return null; 
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} 
		ForgeSvcEntry entry = this.gson.fromJson(jsonProject, ForgeSvcEntry.class);
		ForgeSvcFile[] arrayOfForgeSvcFile = fileterGameVersion(entry.getFiles());
		
	
		if(arrayOfForgeSvcFile.length == 0) return null;

		//on par du pricipe qu'un id plus élevé => version plus récente
		ForgeSvcFile latest = arrayOfForgeSvcFile[0];
		for (int i = 1; i < arrayOfForgeSvcFile.length; i++) {
			if( arrayOfForgeSvcFile[i].getProjectFileId() > latest.getProjectFileId() ) {
				latest = arrayOfForgeSvcFile[i];
			}
		} 
		return latest;
	}

	private ForgeSvcFile[] fileterGameVersion(ForgeSvcFile[] arrayOfForgeSvcFile) {
		return Arrays.stream(arrayOfForgeSvcFile).filter(x -> x.getGameVersion().equals(Main.mcVersion)).toArray(ForgeSvcFile[]::new);
	}
}
