package muksihs.ipfs.photogallery.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
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
	private static class ImgLoadState {
		public int maxFails;
		public boolean loaded;
		public int failCount;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ImgLoadState [maxFails=").append(maxFails).append(", loaded=").append(loaded)
					.append(", failCount=").append(failCount).append("]");
			return builder.toString();
		}
	}

	private final List<ImageData> imageDataUrls;

	private IpfsGatewayEntry putGw = new IpfsGateway().getWritable();

	private Timer timer;

	public StoreImagesInIpfs(List<ImageData> imageDataUrls) {
		this.imageDataUrls = imageDataUrls;
	}

	private Void defer(ScheduledCommand cmd) {
		Scheduler.get().scheduleDeferred(cmd);
		return null;
	}

	private void doImageHeadRequest(ListIterator<ImageData> iImages) {
		if (!iImages.hasNext()) {
			return;
		}
		ImageData image = iImages.next();
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("HEAD", image.getImageUrl());
		xhr.onloadend = (e) -> doThumbHeadRequest(image, iImages);
		xhr.send();
	}

	private void doThumbHeadRequest(ImageData image, ListIterator<ImageData> iImages) {
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("HEAD", image.getThumbUrl());
		xhr.onloadend = (e) -> doImageHeadRequest(iImages);
		xhr.send();
	}

	@Override
	public void execute() {
		fireEvent(new Event.StoreImagesStarted());
		PutState state = new PutState();
		state.setHash(Ipfs.EMPTY_DIR);
		state.setIndex(0);
		state.setImages(new ArrayList<>(this.imageDataUrls));
		state.setIndex(0);
		defer(() -> putImage(state));
	}

	private String getEncodedName(PutState state) {
		String prefix = zeroPadded(state.getImagesSize(), state.getIndex());
		String encodedName = URL.encode(prefix + "/" + state.getImageData().getName());
		return encodedName;
	}

	private Void onImageLoadFail(PutState state, ImgLoadState loadState, HTMLImageElement img) {
		GWT.log("onImageLoadFail: " + loadState.toString());
		if (loadState.loaded) {
			return null;
		}
		if (img.hasAttribute("src")) {
			img.removeAttribute("src");
		}
		loadState.failCount++;
		if (loadState.failCount >= loadState.maxFails) {
			retryPutImage(state); // try again
			return null;
		}
		return null;
	}

	private Void putImage(PutState state) {
		ImageData imageData = state.getImageData();
		GWT.log("putImage: " + imageData.getName());
		imageData.setEncodedName(getEncodedName(state));
		String xhrUrl = putGw.getBaseUrl() + state.getHash() + "/" + imageData.getEncodedName();
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.upload.onprogress = (e) -> {
			if (e.lengthComputable) {
				fireEvent(new Event.SetXhrProgress(100 * e.loaded / e.total));
			}
		};
		xhr.onloadend = (e) -> putThumb(state, xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH), xhr.status);
		xhr.open("PUT", xhrUrl, true);
		xhr.setRequestHeader(Ipfs.HEADER_IPFS_HASH, state.getHash());
		xhr.send(imageData.getImageData());
		return null;
	}

	private Void putNextImage(PutState state) {
		if (!state.hasNext()) {
			fireEvent(new Event.SetXhrProgress(0));
			/*
			 * update all images to use most recent hash, leave gateways as-is though
			 */
			state.setAllIpfsHashes(state.getImageData().getIpfsHash());
			/*
			 * Perform "HEAD" requests against all images and thumbs using the latest
			 * ipfs-hash in the background (sequentially)
			 */
			doImageHeadRequest(state.getImages().listIterator());
			/*
			 * While head requests are firing, go ahead and show post editor
			 */
			fireEvent(new Event.StoreImagesDone());
			return null;
		}
		state.resetFails();
		defer(() -> putImage(state.next()));
		return null;
	}

	private Void putThumb(PutState state, String newHash, double status) {
		if ((int) status == 405) {
			GWT.log("put not supported on this gateway!");
			fireEvent(new Event.AlertMessage("IPFS Gateway Error",
					"This IPFS gateway does not support uploads via PUT!\nPlease select a different gateway!"));
			return null;
		}
		if ((int) status != 201) {
			GWT.log("putImage failed: " + state.getImageData().getName() + " [" + status + "]");
			retryPutImage(state);
			return null;
		}
		GWT.log("putThumb: " + state.getImageData().getName());
		String xhrUrl = putGw.getBaseUrl() + newHash + "/thumb/" + state.getImageData().getEncodedName();
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.upload.onprogress = (e) -> {
			if (e.lengthComputable) {
				fireEvent(new Event.SetXhrProgress(100 * e.loaded / e.total));
			}
		};
		xhr.onloadend = (e) -> verifyThumbImage(state, xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH), xhr.status);
		xhr.open("PUT", xhrUrl, true);
		xhr.setRequestHeader(Ipfs.HEADER_IPFS_HASH, state.getHash());
		xhr.send(state.getImageData().getThumbData());
		return null;
	}

	private void retryPutImage(PutState state) {
		state.incFails();
		if (state.getPutFails() > 5) {
			fireEvent(new Event.AlertMessage("IPFS Gateway Error", "Too Many Upload Failures!\nAborting!"));
			return;
		}
		GWT.log("retryPutImage: " + state.getImageData().getName());
		if (timer != null) {
			GWT.log("timer was running already: " + timer.isRunning());
			timer.cancel();
		}
		timer = new Timer() {
			@Override
			public void run() {
				defer(() -> putImage(state));
				timer = null;
			}
		};
		timer.schedule(1000);
	}

	private Void thumbImageVerified(String fetchGwUrl, String newHash, PutState state, ImgLoadState loadState,
			HTMLImageElement[] imgs, HTMLImageElement img) {
		if (loadState.loaded) {
			return null;
		}
		loadState.loaded = true;
		ImageData imageData = state.getImageData();
		GWT.log("thumbImageVerified: " + imageData.getName());
		for (HTMLImageElement i : imgs) {
			if (i == null) {
				continue;
			}
			if (i.hasAttribute("src")) {
				i.removeAttribute("src");
			}
		}
		imageData.setBaseUrl(fetchGwUrl);
		imageData.setIpfsHash(newHash);
		state.setHash(newHash);
		fireEvent(new Event.AddToPreviewPanel(imageData));
		fireEvent(new Event.SetProgress((1 + state.getIndex()) * 100 / state.getImagesSize()));
		defer(() -> putNextImage(state));
		return null;
	}

	private Void verifyThumbImage(PutState state, String newHash, double status) {
		fireEvent(new Event.SetXhrProgressIndeterminate());
		ImageData imageData = state.getImageData();
		if ((int) status != 201) {
			GWT.log("putThumb failed: " + imageData.getName() + " [" + status + "]");
			retryPutImage(state);
			return null;
		}
		if (newHash == null || newHash.trim().isEmpty()) {
			DomGlobal.console.log("putThumb failed, no ipfs hash: " + imageData.getName() + " [" + status + "]");
			retryPutImage(state);
			return null;
		}
		GWT.log("verifyThumbImage: " + imageData.getName());
		/*
		 * the first successful IMG GET becomes the assigned IPFS HASH for both image
		 * and thumb
		 */
		final HTMLImageElement[] imgs = new HTMLImageElement[4];
		Set<String> already = new HashSet<>();
		ImgLoadState loadState = new ImgLoadState();
		loadState.failCount = 0;
		loadState.loaded = false;
		loadState.maxFails = imgs.length;
		IpfsGateway ipfsGateway = new IpfsGateway();
		for (int iy = 0; iy < imgs.length; iy++) {
			String baseUrl = ipfsGateway.getAnyReadonly().getBaseUrl();
			String url = baseUrl + newHash + "/thumb/" + imageData.getEncodedName();
			if (already.contains(url)) {
				loadState.maxFails--;
				continue;
			}
			already.add(url);
			final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
			imgs[iy] = img;
			img.onabort = (e2) -> onImageLoadFail(state, loadState, img);
			img.onerror = (e2) -> onImageLoadFail(state, loadState, img);
			img.onload = (e2) -> thumbImageVerified(baseUrl, newHash, state, loadState, imgs, img);
			/*
			 * Don't start loading imgs until event hooks and max fail count are set!
			 */
			defer(() -> {
				img.setAttribute("src", url);
				Timer failsafe = new Timer() {
					@Override
					public void run() {
						if (loadState.loaded) {
							return;
						}
						onImageLoadFail(state, loadState, img);
						GWT.log("image load failed - timeout");
					}
				};
				failsafe.schedule(3000);
			});
		}
		return null;
	}

	private String zeroPadded(int length, int ix) {
		ix++;
		int zeroCount = String.valueOf(length).length();
		int digitCount = String.valueOf(ix).length();
		return StringUtils.repeat("0", zeroCount - digitCount) + String.valueOf(ix);
	}
}
