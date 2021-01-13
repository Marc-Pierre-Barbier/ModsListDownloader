package downloader;

public class ProjectInfo {
	private String slug, name, desc;

	public ProjectInfo(String slug, String name, String desc) {
		super();
		this.slug = slug;
		this.name = name;
		this.desc = desc;
	}
	
	public String getDesc() {
		return desc;
	}
	public String getName() {
		return name;
	}
	public String getSlug() {
		return slug;
	}
	
	
}
