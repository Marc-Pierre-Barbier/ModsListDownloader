package downloader.forgeSvc;

import java.net.MalformedURLException;

import downloader.HttpHelper;

public class ForgeSvcFile {
	private String gameVersion;
	private int fileType;
	private int projectFileId;
	private String projectFileName;
	
	public String getGameVersion() {
		return gameVersion;
	}
	
	public int getFileType() {
		return fileType;
	}
	public int getProjectFileId() {
		return projectFileId;
	}
	
	public String getProjectFileName() {
		return projectFileName;
	}

	public String getDownloadUrl(int modID) throws MalformedURLException {
		System.out.println("https://addons-ecs.forgesvc.net/api/v2/addon/"+modID+"/file/"+getProjectFileId());
		return HttpHelper.readStringFromUrl("https://addons-ecs.forgesvc.net/api/v2/addon/"+modID+"/file/"+projectFileId+"/download-url");
	}
	
	public String getMD5(int modID)
	{
		return "https://addons-ecs.forgesvc.net/api/v2/addon/"+modID+"/file/"+getProjectFileId();
	}
}