package muksihs.ipfs.photogallery.shared;

public class ImageUrl {
	private String ipfsHash;
	private String ipfsGateway;
	private String thumb;
	private String image;

	public ImageUrl(String thumb, String image) {
		this.setThumb(thumb);
		this.setImage(image);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ImageUrl)) {
			return false;
		}
		ImageUrl other = (ImageUrl) obj;
		if (image == null) {
			if (other.image != null) {
				return false;
			}
		} else if (!image.equals(other.image)) {
			return false;
		}
		if (ipfsGateway == null) {
			if (other.ipfsGateway != null) {
				return false;
			}
		} else if (!ipfsGateway.equals(other.ipfsGateway)) {
			return false;
		}
		if (ipfsHash == null) {
			if (other.ipfsHash != null) {
				return false;
			}
		} else if (!ipfsHash.equals(other.ipfsHash)) {
			return false;
		}
		if (thumb == null) {
			if (other.thumb != null) {
				return false;
			}
		} else if (!thumb.equals(other.thumb)) {
			return false;
		}
		return true;
	}
	public String getImage() {
		return image;
	}

	public String getImgUrl(){
		StringBuilder sb = new StringBuilder();
		sb.append(ipfsGateway);
		if (!ipfsGateway.endsWith("/")){
			sb.append("/");
		}
		sb.append(ipfsHash);
		if (!ipfsHash.endsWith("/")){
			sb.append("/");
		}
		sb.append(image);
		return sb.toString();
	}

	public String getIpfsGateway() {
		return ipfsGateway;
	}

	public String getIpfsHash() {
		return ipfsHash;
	}

	public String getThumb() {
		return thumb;
	}

	public String getThumbUrl(){
		StringBuilder sb = new StringBuilder();
		sb.append(ipfsGateway);
		if (!ipfsGateway.endsWith("/")){
			sb.append("/");
		}
		sb.append(ipfsHash);
		if (!ipfsHash.endsWith("/")){
			sb.append("/");
		}
		sb.append(thumb);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((ipfsGateway == null) ? 0 : ipfsGateway.hashCode());
		result = prime * result + ((ipfsHash == null) ? 0 : ipfsHash.hashCode());
		result = prime * result + ((thumb == null) ? 0 : thumb.hashCode());
		return result;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setIpfsGateway(String ipfsGateway) {
		this.ipfsGateway = ipfsGateway;
	}

	public void setIpfsHash(String ipfsHash) {
		this.ipfsHash = ipfsHash;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}
}