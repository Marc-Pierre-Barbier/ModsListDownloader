package downloader;

public class CurseForgeModFile extends ProjectInfo {
  	private int projectID;
  	private int fileID;
  	private boolean clientOnly;
	
  	public CurseForgeModFile(ProjectInfo pj, int projectID, int fileID, boolean clientOnly) {
  		this(pj.getSlug(), pj.getName(), pj.getDesc(), projectID, fileID, clientOnly);
  	}

  	public CurseForgeModFile(String slug, String name, String desc, int projectID, int fileID, boolean clientOnly) {
  		super(slug, name, desc);
  		this.projectID = projectID;
  		this.fileID = fileID;
  		this.clientOnly = clientOnly;
  	}

  	public int getFileID() {
  		return this.fileID;
  	}

  	public int getProjectID() {
  		return this.projectID;
  	}

  	public boolean isClientOnly() {
  		return this.clientOnly;
  	}
}
