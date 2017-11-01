package muksihs.ipfs.photogallery.client;

import java.util.List;

import muksihs.ipfs.photogallery.shared.ImageData;

public class PutState {
	private int putFails;
	private String hash;
	private List<ImageData> images;
	private int index;

	public String getHash() {
		return hash;
	}

	public ImageData getImageData() {
		return images.get(index);
	}

	public List<ImageData> getImages() {
		return images;
	}

	public int getImagesSize() {
		return this.images != null ? this.images.size() : 0;
	}

	public int getIndex() {
		return index;
	}

	public int getPutFails() {
		return putFails;
	}

	public boolean hasNext() {
		return (index + 1) < images.size();
	}

	public void incFails() {
		setPutFails(getPutFails() + 1);
	}

	public PutState next() {
		index++;
		return this;
	}

	public void resetFails() {
		setPutFails(0);
	}

	public PutState setAllIpfsHashes(String ipfsHash) {
		for (ImageData image : images) {
			image.setIpfsHash(ipfsHash);
		}
		return this;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setImages(List<ImageData> imgs) {
		this.images = imgs;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setPutFails(int putFails) {
		this.putFails = putFails;
	}
}