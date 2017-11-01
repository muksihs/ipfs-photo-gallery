package muksihs.ipfs.photogallery.shared;

import java.util.List;

public class GalleryInfo {

	private String description;
	private String title;
	private List<String> tags;

	public GalleryInfo setDescription(String value) {
		this.description=value;
		return this;
	}

	public GalleryInfo setTitle(String value) {
		this.title=value;
		return this;
	}
	
	public GalleryInfo setTags(List<String> value) {
		this.tags=value;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getTags() {
		return tags;
	}
	
}