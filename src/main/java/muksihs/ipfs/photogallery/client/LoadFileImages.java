package muksihs.ipfs.photogallery.client;

import com.google.gwt.core.client.GWT;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.FileReader;
import elemental2.dom.FileReader.ResultUnionType;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLImageElement;
import muksihs.ipfs.photogallery.shared.Consts;
import muksihs.ipfs.photogallery.shared.ImageData;
import muksihs.ipfs.photogallery.ui.GlobalEventBus;

public class LoadFileImages implements GlobalEventBus {
	public void load(FileList files) {
		loadNextDataUrl(files, 0);
	}

	// private List<ImageData> dataUrls=new ArrayList<>();
	protected Void loadNextDataUrl(FileList files, int ix) {
		if (ix >= files.length) {
			fireEvent(new Event.AddImagesDone());
			return null;
		}
		GWT.log("loadDataUrl: " + files.getAt(ix).name);
		FileReader reader = new FileReader();
		File file = files.getAt(ix);
		reader.onabort = (e) -> loadNextDataUrl(files, ix + 1);// skip un-readable file
		reader.onerror = (e) -> loadNextDataUrl(files, ix + 1);// skip un-readable file
		reader.onloadend = (e) -> onFileLoaded(files, ix, reader.result);
		reader.readAsDataURL(file.slice());
		return null;
	}

	public Void onFileLoaded(FileList files, int ix, ResultUnionType result) {
		File file = files.getAt(ix);
		HTMLImageElement image = (HTMLImageElement) DomGlobal.document.createElement("img");
		image.onabort = (e) -> loadNextDataUrl(files, ix + 1);// skip non-image file
		image.onerror = (e) -> loadNextDataUrl(files, ix + 1);// skip non-image file
		image.onload = (e) -> {
			return createThumbnail(files, ix, new ImageData(file.slice(), null, file.name), image);
		};
		image.src = result.asString();
		return null;
	}

	public Void createThumbnail(FileList files, int ix, ImageData imageData, HTMLImageElement image) {
		double scale;
		double w = image.width;
		double h = image.height;
		/*
		 * size to fit
		 */
		if (w > h) {
			scale = Consts.maxSize / w;
		} else {
			scale = Consts.maxSize / h;
		}
		// draw thumbnail image
		HTMLCanvasElement canvas = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
		canvas.width = image.width * scale;
		canvas.height = image.height * scale;
		CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");
		ctx.drawImage(image, 0, 0, image.width * scale, image.height * scale);
		String mime = image.src.contains(";base64,iVBOR") ? "image/png" : "image/jpeg";
		canvas.toBlob((blob) -> {
			fireEvent(new Event.ImageDataAdded(imageData.setThumbData(blob)));
			loadNextDataUrl(files, ix + 1);
			return null;
		}, mime, Consts.jpgQuality);
		return null;
	}

	public native String createObjectURL(elemental2.dom.Blob bb) /*-{
		return $wnd.URL.createObjectURL(bb);
	}-*/;
}
