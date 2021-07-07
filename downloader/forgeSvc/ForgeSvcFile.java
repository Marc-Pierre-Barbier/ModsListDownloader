package downloader.forgeSvc;

import downloader.HttpHelper;
import java.net.MalformedURLException;

public class ForgeSvcFile {
	private String gameVersion;
	private int fileType;
	private int projectFileId;
	private String projectFileName;
	
	public String getGameVersion() {
		return this.gameVersion;
	}
	
	public int getFileType() {
		return this.fileType;
	}
	
	public int getProjectFileId() {
		return this.projectFileId;
	}
	
	public String getProjectFileName() {
		return this.projectFileName;
	}
	
	public String getDownloadUrl(int modID) throws MalformedURLException {
		return HttpHelper.readStringFromUrl("https://addons-ecs.forgesvc.net/api/v2/addon/" + modID + "/file/" + this.projectFileId + "/download-url");
	}
}
