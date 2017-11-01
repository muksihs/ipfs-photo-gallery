package muksihs.ipfs.photogallery.shared;

import elemental2.dom.Blob;

public class ImageData {
	private Blob imageData;
	private Blob thumbData;
	private String encodedName;
	private String ipfsHash;
	private String baseUrl;
	private String name;

	public ImageData() {
	}

	public ImageData(Blob imageData, Blob thumbData, String name) {
		this.imageData = imageData;
		this.thumbData = thumbData;
		this.name = name;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getEncodedName() {
		return encodedName;
	}

	public Blob getImageData() {
		return imageData;
	}

	public String getIpfsHash() {
		return ipfsHash;
	}

	public String getName() {
		return name;
	}

	public Blob getThumbData() {
		return thumbData;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public ImageData setEncodedName(String encodedName) {
		this.encodedName = encodedName;
		return this;
	}

	public ImageData setImageData(Blob imageData) {
		this.imageData = imageData;
		return this;
	}

	public ImageData setIpfsHash(String ipfsHash) {
		this.ipfsHash = ipfsHash;
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
}
