package muksihs.ipfs.photogallery.shared;

public interface View {

	void setReady(boolean ready);

	void setSteemitHtml(String text);

	void setProgress(double d);
	void setProgressIndeterminate();

	void setFileText(String msg);

	void setIpfsFolderLink(String finalUrl);

	void setPreviewHtml(String steemitHtml);

	void setController(PhotoGalleryController photoGalleryApp);

	void alert(String string);
}
