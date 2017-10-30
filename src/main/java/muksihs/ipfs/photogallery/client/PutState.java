package muksihs.ipfs.photogallery.client;

import java.util.List;

import muksihs.ipfs.photogallery.shared.ImageData;

public class PutState {
	public int getI() {
		return i;
	}

	private String hash;
	public String getHash() {
		return hash;
	}
	
	public int size() {
		return this.imgs!=null?this.imgs.size():0;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setImgs(List<ImageData> imgs) {
		this.imgs = imgs;
	}

	public void setI(int i) {
		this.i = i;
	}

	private List<ImageData> imgs;
	private int i;

	public boolean hasNext() {
		return (i+1)<imgs.size();
	}
	
	public ImageData img() {
		return imgs.get(i);
	}

	public PutState next() {
		i++;
		return this;
	}
}