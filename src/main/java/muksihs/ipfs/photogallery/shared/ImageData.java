package muksihs.ipfs.photogallery.shared;

import elemental2.dom.Blob;

public class ImageData {
	private Blob imageData;
	private Blob thumbData;
	private String imageUrl;
	private String thumbUrl;
	private String name;

	public ImageData() {
	}

	public ImageData(Blob imageData, Blob thumbData, String name) {
		this.imageData = imageData;
		this.thumbData = thumbData;
		this.name = name;
	}

	public Blob getImageData() {
		return imageData;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getName() {
		return name;
	}

	public Blob getThumbData() {
		return thumbData;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public ImageData setImageData(Blob imageData) {
		this.imageData = imageData;
		return this;
	}

	public ImageData setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
		return this;
	}

	public ImageData setName(String name) {
		this.name = name;
		return this;
	}

	public ImageData setThumbData(Blob thumbData) {
		this.thumbData = thumbData;
		return this;
	}

	public ImageData setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
		return this;
	}
}
