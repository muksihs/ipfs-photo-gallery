package muksihs.ipfs.photogallery.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;

import elemental2.dom.Blob;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLCanvasElement.ToBlobCallbackFn;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.XMLHttpRequest;
import muksihs.ipfs.photogallery.shared.Consts;
import muksihs.ipfs.photogallery.shared.ImageData;
import muksihs.ipfs.photogallery.shared.Ipfs;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.shared.IpfsGatewayEntry;
import muksihs.ipfs.photogallery.ui.GlobalEventBus;

public class StoreImagesInIpfs implements GlobalEventBus, ScheduledCommand {
	private final List<ImageData> imageDataUrls;

	public StoreImagesInIpfs(List<ImageData> imageDataUrls) {
		this.imageDataUrls = imageDataUrls;
	}

	@Override
	public void execute() {
		fireEvent(new Event.StoreImagesStarted());
		PutState state = new PutState();
		state.setHash(Ipfs.EMPTY_DIR);
		state.setI(0);
		state.setImgs(new ArrayList<>(this.imageDataUrls));
		storeThumbnailImage(state);
	}

	private String zeroPadded(int length, int ix) {
		ix++;
		int zeroCount = String.valueOf((int) length).length();
		int digitCount = String.valueOf((int) ix).length();
		return StringUtils.repeat("0", zeroCount - digitCount) + String.valueOf((int) ix);
	}

	private static class ImgLoadState {
		public int maxFails;
		public boolean loaded;
		public int failCount;
	}

	private void getImageBlob(String base64, ToBlobCallbackFn callback) {
		if (!base64.startsWith("data:")) {
			GWT.log("getImageBlob with non-data url: " + base64);
		}
		GWT.log("getImageBlob");
		final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
		img.src = base64;
		HTMLCanvasElement canvas = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
		canvas.width = img.naturalWidth;
		canvas.height = img.naturalHeight;
		CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");
		ctx.drawImage(img, 0, 0);
		String mime = img.src.contains(";base64,iVBOR") ? "image/png" : "image/jpeg";
		canvas.toBlob(callback, mime, Consts.jpgQuality);
	}

	private Void storeThumbnailImage(PutState state) {
		if (!state.hasNext()) {
			fireEvent(new Event.IpfsLoadDone());
			return null;
		}
		GWT.log("storeThumbnailImage");
		String prefix = zeroPadded(state.size(), state.getI());
		String encodedName = URL.encode(prefix + "-" + state.img().getCaption());
		String imgLocation = "/thumb/" + encodedName;
		try {
			getImageBlob(state.img().getThumbDataUrl(), (blob) -> putThumbImage(state, imgLocation, blob));
		} catch (Exception e) {
			GWT.log("Exception: " + e.getMessage(), e);
		}
		return null;
	}

	private Void putThumbImage(PutState state, String imgLocation, Blob img) {
		GWT.log("putThumbImage");
		IpfsGatewayEntry putGw = new IpfsGateway().getWritable();
		String baseUrl = putGw.getBaseUrl();
		String xhrUrl = baseUrl.replace(":hash", state.getHash()) + imgLocation;
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("PUT", xhrUrl, true);
		xhr.onloadend = (e) -> verifyThumbImage(state, xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH), imgLocation,
				xhr.status);
		xhr.send(img);
		return null;
	}

	private Void verifyThumbImage(PutState state, String newHash, String imgLocation, double status) {
		if (newHash == null || newHash.trim().isEmpty()) {
			retryPutImage(state);
			return null;
		}
		if (status != 201) {
			GWT.log("PUT thumb failed.");
			retryPutImage(state);
			return null;
		}
		GWT.log("verifyThumbImage");
		// the first successful GET becomes the assigned URL
		final HTMLImageElement[] imgs = new HTMLImageElement[4];
		Set<String> already = new HashSet<>();
		ImgLoadState loadState = new ImgLoadState();
		loadState.failCount = 0;
		loadState.loaded = false;
		loadState.maxFails = imgs.length;
		IpfsGateway ipfsGateway = new IpfsGateway();
		for (int iy = 0; iy < imgs.length; iy++) {
			IpfsGatewayEntry fetchGw = ipfsGateway.getAnyReadonly();
			String url = fetchGw.getBaseUrl().replace(":hash", newHash) + imgLocation;
			if (already.contains(url)) {
				loadState.maxFails--;
				continue;
			}
			already.add(url);
			final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
			imgs[iy] = img;
			img.onabort = (e2) -> onImageLoadFail(state, loadState, img);
			img.onerror = (e2) -> onImageLoadFail(state, loadState, img);
			img.onload = (e2) -> onThumbImageLoad(newHash, state, loadState, imgs, img);
			img.setAttribute("src", url);
		}
		return null;
	}

	private Void onThumbImageLoad(String newHash, PutState state, ImgLoadState loadState, HTMLImageElement[] imgs,
			HTMLImageElement img) {
		GWT.log("onThumbImageLoad");
		loadState.loaded = true;
		state.img().setThumbUrl(img.src);
		for (HTMLImageElement i : imgs) {
			if (i.hasAttribute("src")) {
				i.removeAttribute("src");
			}
		}
		state.setHash(newHash);
		storeImage(state);
		return null;
	}

	private Void storeImage(PutState state) {
		GWT.log("storeImage");
		String prefix = zeroPadded(state.size(), state.getI());
		String encodedName = URL.encode(prefix + "-" + state.img().getCaption());
		String imgLocation = "/" + encodedName;
		getImageBlob(state.img().getImageDataUrl(), (blob) -> putImage(state, imgLocation, blob));
		return null;
	}

	private Void putImage(PutState state, String imgLocation, Blob blob) {
		GWT.log("putImage");
		IpfsGatewayEntry putGw = new IpfsGateway().getWritable();
		String baseUrl = putGw.getBaseUrl();
		String xhrUrl = baseUrl.replace(":hash", state.getHash()) + imgLocation;
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("PUT", xhrUrl, true);
		xhr.onloadend = (e) -> verifyImage(state, xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH), imgLocation,
				xhr.status);
		xhr.send(blob);
		return null;
	}

	private Void verifyImage(PutState state, String newHash, String imgLocation, double status) {
		if (newHash == null || newHash.trim().isEmpty()) {
			retryPutImage(state);
			return null;
		}
		if (status != 201) {
			GWT.log("PUT image failed.");
			retryPutImage(state);
			return null;
		}
		GWT.log("verifyImage");
		// the first successful GET becomes the assigned URL
		final HTMLImageElement[] imgs = new HTMLImageElement[4];
		Set<String> already = new HashSet<>();
		ImgLoadState loadState = new ImgLoadState();
		loadState.failCount = 0;
		loadState.loaded = false;
		loadState.maxFails = imgs.length;
		IpfsGateway ipfsGateway = new IpfsGateway();
		for (int iy = 0; iy < imgs.length; iy++) {
			IpfsGatewayEntry fetchGw = ipfsGateway.getAnyReadonly();
			String url = fetchGw.getBaseUrl().replace(":hash", newHash) + imgLocation;
			if (already.contains(url)) {
				loadState.maxFails--;
				continue;
			}
			already.add(url);
			final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
			imgs[iy] = img;
			img.onabort = (e2) -> onImageLoadFail(state, loadState, img);
			img.onerror = (e2) -> onImageLoadFail(state, loadState, img);
			img.onload = (e2) -> onImageLoad(newHash, state, loadState, imgs, img);
			img.setAttribute("src", url);
		}
		return null;
	}

	private Void onImageLoad(String newHash, PutState state, ImgLoadState loadState, HTMLImageElement[] imgs,
			HTMLImageElement img) {
		GWT.log("onImageLoad");
		loadState.loaded = true;
		state.img().setImageUrl(img.src);
		for (HTMLImageElement i : imgs) {
			if (i != null) {
				i.removeAttribute("src");
			}
		}
		fireEvent(new Event.AddToPreviewPanel(state.img().getThumbUrl(), state.img().getCaption()));
		state.setHash(newHash);
		storeThumbnailImage(state.next());
		return null;
	}

	private Void onImageLoadFail(PutState state, ImgLoadState loadState, HTMLImageElement img) {
		if (loadState.loaded) {
			return null;
		}
		img.removeAttribute("src");
		loadState.failCount++;
		if (loadState.failCount >= loadState.maxFails) {
			storeThumbnailImage(state); // try again
		}
		return null;
	}

	private void retryPutImage(PutState state) {
		GWT.log("retryPutImage: " + state.img().getCaption());
		new Timer() {
			@Override
			public void run() {
				storeThumbnailImage(state);
			}
		}.schedule(500);
	}
}
