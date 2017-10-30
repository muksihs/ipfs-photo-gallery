package muksihs.ipfs.photogallery.client;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.URL;

import elemental2.dom.RTCDataChannel.SendDataUnionType;
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
		storeThumbnailImage(Ipfs.EMPTY_DIR, imageDataUrls, 0);
	}

	private String zeroPadded(int length, int ix) {
		ix++;
		int zeroCount = String.valueOf((int) length).length();
		int digitCount = String.valueOf((int) ix).length();
		return StringUtils.repeat("0", zeroCount - digitCount) + String.valueOf((int) ix);
	}

	private Void storeThumbnailImage(String hash, List<ImageData> imgs, int i) {
		IpfsGatewayEntry putGw = new IpfsGateway().getWritable();
		String prefix = zeroPadded((int) imgs.size(), i);
		String baseUrl = putGw.getBaseUrl();
		String encodedName = URL.encode(prefix + "-" + imgs.get(i).getCaption());
		String xhrUrl = baseUrl.replace(":hash", hash) + "/thumb/" + encodedName;
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("PUT", xhrUrl, true);
		xhr.onloadend = (e) -> onThumbPutDone(hash, imgs, i, xhr.status, xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH));
		final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
		img.onabort = (e2) -> storeThumbnailImage(hash, imgs, i+1); //bogus data src url? skip it!
		img.onerror = (e2) -> storeThumbnailImage(hash, imgs, i+1); //bogus data src url? skip it!
		//TODO: after img load is complete, do a put to the IPFS gateway
		//TODO: after put, find an IPFS host that has the data and set the URL in the imagedata object
		//TODO: after repeating this with full sized image, update preview with thumbnail and update percent on progress bar.
//		img.onload = (e2) -> onTestLoadImage(files, ix, newHash, imgs, state, fetchGw, picUrl, thumbUrl);
//		img.setAttribute("src", thumbUrl);
		return null;
	}

	private Void onThumbPutDone(String hash, List<ImageData> imgs, int i, double status, String newHash) {

		return null;
	}

	private void storeImage(String hash, List<ImageData> imgs, int i) {

		String newHash = hash;
		storeThumbnailImage(newHash, imgs, i);
	}
}
