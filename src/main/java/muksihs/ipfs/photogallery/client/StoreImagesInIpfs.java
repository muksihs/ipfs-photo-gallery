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

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.XMLHttpRequest;
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
		putImage(state);
	}
	private IpfsGatewayEntry putGw = new IpfsGateway().getWritable();
	
	private String getEncodedName(PutState state) {
		String prefix = zeroPadded(state.size(), state.getI());
		String encodedName = URL.encode(prefix + "-" + state.img().getName());
		return encodedName;
	}
	private Void putImage(PutState state) {
		if (!state.hasNext()) {
			fireEvent(new Event.IpfsLoadDone());
			return null;
		}
		GWT.log("putImage");
		String baseUrl = putGw.getBaseUrl();
		String xhrUrl = baseUrl.replace(":hash", state.getHash()) + "/"+getEncodedName(state);
		GWT.log(" - "+xhrUrl);
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("PUT", xhrUrl, true);
		xhr.onloadend = (e) -> putThumb(state, xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH),
				xhr.status);
		xhr.send(state.img().getImageData());
		return null;
	}
	private Void putThumb(PutState state, String newHash, double status) {
		if (status != 201) {
			GWT.log("putImage failed.");
			retryPutImage(state);
			return null;
		}
		GWT.log("putThumb");
		String baseUrl = putGw.getBaseUrl();
		String xhrUrl = baseUrl.replace(":hash", newHash) + "/thumb/"+getEncodedName(state);
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("PUT", xhrUrl, true);
		xhr.onloadend = (e) -> verifyThumbImage(state, xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH),
				xhr.status);
		xhr.send(state.img().getImageData());
		return null;
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

	private Void verifyThumbImage(PutState state, String newHash, double status) {
		if (status != 201) {
			GWT.log("putThumb failed.");
			retryPutImage(state);
			return null;
		}
		if (newHash == null || newHash.trim().isEmpty()) {
			GWT.log("putThumb failed. (no hash)");
			retryPutImage(state);
			return null;
		}
		GWT.log("verifyThumbImage");
		// the first successful IMG GET becomes the assigned URL for both image and thumb
		final HTMLImageElement[] imgs = new HTMLImageElement[4];
		Set<String> already = new HashSet<>();
		ImgLoadState loadState = new ImgLoadState();
		loadState.failCount = 0;
		loadState.loaded = false;
		loadState.maxFails = imgs.length;
		IpfsGateway ipfsGateway = new IpfsGateway();
		for (int iy = 0; iy < imgs.length; iy++) {
			IpfsGatewayEntry fetchGw = ipfsGateway.getAnyReadonly();
			String url = fetchGw.getBaseUrl().replace(":hash", newHash) + "/thumb/"+getEncodedName(state);
			if (already.contains(url)) {
				loadState.maxFails--;
				continue;
			}
			already.add(url);
			final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
			imgs[iy] = img;
			img.onabort = (e2) -> onImageLoadFail(state, loadState, img);
			img.onerror = (e2) -> onImageLoadFail(state, loadState, img);
			img.onload = (e2) -> onThumbImageLoad(fetchGw.getBaseUrl(), newHash, state, loadState, imgs, img);
			img.setAttribute("src", url);
		}
		return null;
	}

	private Void onThumbImageLoad(String fetchGwUrl, String newHash, PutState state, ImgLoadState loadState, HTMLImageElement[] imgs,
			HTMLImageElement img) {
		GWT.log("onThumbImageLoad");
		loadState.loaded = true;
		state.img().setThumbUrl(img.src);
		state.img().setImageUrl(fetchGwUrl.replace(":hash", newHash)+"/"+getEncodedName(state));
		for (HTMLImageElement i : imgs) {
			if (i.hasAttribute("src")) {
				i.removeAttribute("src");
			}
		}
		state.setHash(newHash);
		fireEvent(new Event.AddToPreviewPanel(state.img()));
		putImage(state.next());
		return null;
	}

	private Void onImageLoadFail(PutState state, ImgLoadState loadState, HTMLImageElement img) {
		if (loadState.loaded) {
			return null;
		}
		img.removeAttribute("src");
		loadState.failCount++;
		if (loadState.failCount >= loadState.maxFails) {
			retryPutImage(state); // try again
		}
		return null;
	}

	private void retryPutImage(PutState state) {
		GWT.log("retryPutImage: " + state.img().getName());
		new Timer() {
			@Override
			public void run() {
				putImage(state);
			}
		}.schedule(500);
	}
}
