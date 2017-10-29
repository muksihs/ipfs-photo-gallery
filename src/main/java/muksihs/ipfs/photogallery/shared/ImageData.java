package muksihs.ipfs.photogallery.shared;

public class ImageData {
	private String imageDataUrl;
	private String thumbDataUrl;
	private String caption;
	public ImageData() {
	}
	public ImageData(String imageDataurl, String thumbDataUrl, String caption) {
		this.imageDataUrl=imageDataurl;
		this.thumbDataUrl=thumbDataUrl;
		this.caption=caption;
	}
	public String getImageDataUrl() {
		return imageDataUrl;
	}
	public void setImageDataUrl(String imageDataUrl) {
		this.imageDataUrl = imageDataUrl;
	}
	public String getThumbDataUrl() {
		return thumbDataUrl;
	}
	public void setThumbDataUrl(String thumbDataUrl) {
		this.thumbDataUrl = thumbDataUrl;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
}
