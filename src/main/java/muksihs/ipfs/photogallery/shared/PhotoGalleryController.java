package muksihs.ipfs.photogallery.shared;

import elemental2.dom.FileList;

public interface PhotoGalleryController {
	void wantsHtmlDisplayed();

	void uploadImages(FileList files);
	
	void wantsColumns(int count);

	void setView(View view);

	void wantsNsfw(Boolean value);
}
