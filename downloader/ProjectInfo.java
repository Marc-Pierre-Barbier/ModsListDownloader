package downloader;

public class ProjectInfo {
	private String slug;
	
	private String name;
	
	private String desc;
	
	public ProjectInfo(String slug, String name, String desc) {
		this.slug = slug;
		this.name = name;
		this.desc = desc;
	}
	
	public String getDesc() {
		return this.desc;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getSlug() {
		return this.slug;
	}
}
