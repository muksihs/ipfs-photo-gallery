package muksihs.ipfs.photogallery.shared;

import java.util.List;

public class GalleryInfo {

	private String description;
	private String title;
	private List<String> tags;

	public String getDescription() {
		return description;
	}

	public List<String> getTags() {
		return tags;
	}

	public String getTitle() {
		return title;
	}

	public GalleryInfo setDescription(String value) {
		this.description = value;
		return this;
	}

	public GalleryInfo setTags(List<String> value) {
		this.tags = value;
		return this;
	}

	public GalleryInfo setTitle(String value) {
		this.title = value;
		return this;
	}

}