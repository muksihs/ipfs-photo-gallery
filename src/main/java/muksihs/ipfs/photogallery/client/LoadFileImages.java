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

	private Void createThumbnail(FileList files, int ix, ImageData imageData, HTMLImageElement image) {
		double scale;
		double w = image.width;
		double h = image.height;
		/*
		 * size to fit
		 */
		if (w > h) {
			scale = Consts.thumbMaxSize / w;
		} else {
			scale = Consts.thumbMaxSize / h;
		}
		// draw thumbnail image
		HTMLCanvasElement canvas = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
		canvas.width = image.width * scale;
		canvas.height = image.height * scale;
		CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");
		ctx.drawImage(image, 0, 0, image.width * scale, image.height * scale);
		String mime = image.src.contains(";base64,iVBOR") ? "image/png" : "image/jpeg";
		mime = image.src.contains(";base64,R0lGODl") ? "image/gif" : mime;
		canvas.toBlob((blob) -> {
			fireEvent(new Event.ImageDataAdded(imageData.setThumbData(blob)));
			loadNextDataUrl(files, ix + 1);
			return null;
		}, mime, Consts.thumbJpgQuality);
		return null;
	}

	public void load(FileList files) {
		loadNextDataUrl(files, 0);
	}

	private Void loadNextDataUrl(FileList files, int ix) {
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

	private Void onFileLoaded(FileList files, int ix, ResultUnionType result) {
		File file = files.getAt(ix);
		HTMLImageElement image = (HTMLImageElement) DomGlobal.document.createElement("img");
//		image.setAttribute("style", "orientation: from-image;");
		image.onabort = (e) -> loadNextDataUrl(files, ix + 1);// skip non-image file
		image.onerror = (e) -> loadNextDataUrl(files, ix + 1);// skip non-image file
		image.onload = (e) -> resizeImage(files, ix, new ImageData(file.slice(), null, file.name), image);
		image.src = result.asString();
		return null;
	}

	private Void resizeImage(FileList files, int ix, ImageData imageData, HTMLImageElement image) {
		double scale;
		double w = image.width;
		double h = image.height;
		/*
		 * scale image down
		 */
		if (w > h) {
			scale = Consts.imageMaxSize / w;
		} else {
			scale = Consts.imageMaxSize / h;
		}
		/*
		 * if image is already small enough or is a GIF, skip resize step
		 */
		if (scale > 1 || imageData.getName().toLowerCase().endsWith(".gif")) {
			GWT.log("resizeImage: skip");
			createThumbnail(files, ix, imageData, image);
			return null;
		}
		// resize image
		HTMLCanvasElement canvas = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
		canvas.width = image.width * scale;
		canvas.height = image.height * scale;
		CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");
		ctx.drawImage(image, 0, 0, image.width * scale, image.height * scale);
		String mime = image.src.contains(";base64,iVBOR") ? "image/png" : "image/jpeg";
		mime = image.src.contains(";base64,R0lGODl") ? "image/gif" : mime;
		canvas.toBlob((resized) -> createThumbnail(files, ix, imageData.setImageData(resized), image), mime,
				Consts.imageJpgQuality);
		return null;
	}
}
