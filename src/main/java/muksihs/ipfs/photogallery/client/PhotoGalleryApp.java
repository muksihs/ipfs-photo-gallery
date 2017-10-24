package muksihs.ipfs.photogallery.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

import elemental2.dom.Blob;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.FileReader;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequest;
import muksihs.ipfs.photogallery.shared.Consts;
import muksihs.ipfs.photogallery.shared.ImageUrl;
import muksihs.ipfs.photogallery.shared.Ipfs;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.shared.IpfsGatewayEntry;
import muksihs.ipfs.photogallery.shared.PhotoGalleryController;
import muksihs.ipfs.photogallery.shared.View;

public class PhotoGalleryApp implements PhotoGalleryController {
	private View view;

	private int columns = 4;
	private IpfsGatewayCache ipfsGwCache;

	public void wantsColumns(int count) {
		columns = count;
	}

	public PhotoGalleryApp(View view) {
		ipfsGwCache = IpfsGatewayCache.get();
		this.view=view;
		this.view.setController(this);
		this.view.setReady(false);
		Scheduler.get().scheduleFixedDelay(() -> {
			boolean ready = IpfsGateway.isReady();
			this.view.setReady(ready);
			if (ready) {
				Scheduler.get().scheduleDeferred(() -> updatePreview());
			}
			return !ready;
		}, 500);
	}

	private void updatePreview() {
		view.setPreviewHtml(steemitHtml());
	}

	private List<ImageUrl> pics = new ArrayList<>();

	private String steemitHtml() {
		final String placeholder = Consts.PLACEHOLDER;
		int perRow;
		String template = PostingTemplates.getInstance().templates().getText();
		perRow = columns;
		switch (columns) {
		case 1:
			template = StringUtils.substringBetween(template, "<!-- 1 -->", "<!--");
			break;
		case 2:
			template = StringUtils.substringBetween(template, "<!-- 2 -->", "<!--");
			break;
		case 4:
			template = StringUtils.substringBetween(template, "<!-- 4 -->", "<!--");
			break;
		case 8:
			template = StringUtils.substringBetween(template, "<!-- 8 -->", "<!--");
			break;
		default:
			perRow = 2;
			template = StringUtils.substringBetween(template, "<!-- 2 -->", "<!--");
		}
		Iterator<ImageUrl> iPics = pics.iterator();
		StringBuilder previewHtml = new StringBuilder();
		String tmp = template;
		int cell = 0;
		while (iPics.hasNext()) {
			cell++;
			if (cell > perRow) {
				previewHtml.append(tmp);
				tmp = template;
				cell = 1;
			}
			ImageUrl pic = iPics.next();
			// set img src and a href values.
			tmp = tmp.replace("href=\"_IMG" + cell + "_", "href=\"" + pic.getImage());
			tmp = tmp.replace("src=\"_IMG" + cell + "_", "src=\"" + pic.getThumb());
			// set alt text values.
			String name = StringUtils.substringAfterLast(pic.getImage(), "/");
			String basename = StringUtils.substringBeforeLast(name, ".");
			tmp = tmp.replace("_ALT" + cell + "_", basename);
		}
		if (cell <= perRow) {
			while (cell <= perRow) {
				cell++;
				tmp = tmp.replace("_IMG" + cell + "_",
						new IpfsGateway().getAny().getBaseUrl().replace(":hash", placeholder));
				tmp = tmp.replace("_ALT" + cell + "_", "EMPTY");
			}
			previewHtml.append(tmp);
		}
		return "<div>" + previewHtml.toString() + "</div>";
	}

	@Override
	public void wantsHtmlDisplayed() {
		String aHref = "<a href='" + GWT.getHostPageBaseURL() + "'>";
		String text = "<html>\n";
		if (nsfw) {
			text += "<div class='pull-left'><img src='"+Consts.NSFW+"'/></div>\n";
		}
		text += steemitHtml() //
				+ "\n" //
				+ "<div class='pull-right'>\n" //
				+ aHref + "\nMuksihs' IPFS Photo Gallery Maker\n</a>\n" //
				+ "<br/>@muksihs\n" //
				+ "</div>" //
				+ "\n</html>";
		text = text.replace("\t", "  ");
		text = text.replaceAll("\n+", "\n");
		view.setSteemitHtml(text);
	}

	@Override
	public void uploadImages(FileList files) {
		GWT.log("uploadImages: " + files.length);
		Scheduler.get().scheduleDeferred(() -> uploadImage(Ipfs.EMPTY_DIR, files, 0));
	}

	private IpfsGatewayEntry last = new IpfsGateway().getAny();

	private Boolean nsfw;

	private String wif;

	private String username;

	private void uploadImage(String hash, FileList files, int ix) {
		if (ix >= files.length) {
			view.setFileText("Upload complete.");
			view.setProgress(0);
			GWT.log("DONE PUTTING IMAGES.");
			String finalUrl = last.getBaseUrl().replace(":hash", hash) + "/";
			view.setIpfsFolderLink(finalUrl);
			return;
		}
		_putThumb(hash, files, ix);
	}

	private void _putThumb(String hash, FileList files, int ix) {
		GWT.log("_putThumb: " + files.getAt(ix).name);
		FileReader reader = new FileReader();
		File file = files.getAt(ix);

		view.setFileText(file.name + " (Thumbnail) [" + (ix + 1) + " of " + files.length + "]");
		reader.onabort = (e) -> retryPutImage(hash, files, ix);
		reader.onerror = (e) -> retryPutImage(hash, files, ix);
		reader.onloadend = (e) -> onFileLoaded(hash, files, ix, reader);
		reader.readAsDataURL(file.slice());
	}

	public Object onFileLoaded(String hash, FileList files, int ix, FileReader r) {
		HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
		img.onabort = (e2) -> retryPutImage(hash, files, ix+1);//skip non-image file
		img.onerror = (e2) -> retryPutImage(hash, files, ix+1);//skip non-image file
		img.onload = (e2) -> createThumbnail(hash, files, ix, img);
		img.src = r.result.asString();
		return null;
	}

	public Void createThumbnail(String hash, FileList files, int ix, HTMLImageElement thumbImg) {
		double scale;
		double w = thumbImg.width;
		double h = thumbImg.height;
		/*
		 * size to fit
		 */
		if (w > h) {
			scale = Consts.maxSize / w;
		} else {
			scale = Consts.maxSize / h;
		}
		// draw thumbnail image
		HTMLCanvasElement thumb = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
		thumb.width = thumbImg.width * scale;
		thumb.height = thumbImg.height * scale;
		CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) thumb.getContext("2d");
		ctx.drawImage(thumbImg, 0, 0, thumbImg.width * scale, thumbImg.height * scale);
		String mime = thumbImg.src.contains(";base64,iVBOR") ? "image/png" : "image/jpeg";
		Image w2 = new Image();
		RootPanel.get().add(w2);
		w2.setUrl(thumbImg.src);
		thumb.toBlob((p0) -> uploadThumbnail(hash, files, ix, p0), mime, Consts.jpgQuality);
		return null;
	}

	public Object uploadThumbnail(String hash, FileList files, int ix, Blob blob) {
		File file = files.getAt(ix);
		IpfsGatewayEntry putGw = new IpfsGateway().getWritable();
		String prefix = zeroPadded((int) files.length, ix);
		String baseUrl = putGw.getBaseUrl();
		String encodedName = URL.encode(prefix + "-" + file.name);
		String xhrUrl = baseUrl.replace(":hash", hash) + "/thumb/" + encodedName;
		GWT.log("uploadThumbnail: " + xhrUrl);
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("PUT", xhrUrl, true);
		xhr.onloadend = (e) -> onThumbPutDone(hash, files, ix, xhr.status,
				xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH));
		xhr.send(blob);
		return null;
	}

	public void onThumbPutDone(String hash, FileList files, int ix, double xhrStatus, String newHash) {
		view.setProgressIndeterminate();
		if (newHash == null || newHash.trim().isEmpty()) {
			retryPutImage(hash, files, ix);
			return;
		}
		if (xhrStatus != 201) {
			GWT.log("PUT thumb failed.");
			view.setProgress(0);
			ipfsGwCache.forceExpireGateways();
			retryPutImage(hash, files, ix);
			return;
		}
		_putImage(newHash, files, ix);
	}

	public Object retryPutImage(String hash, FileList files, int ix) {
		GWT.log("retryImage: " + files.getAt(ix).name);
		new Timer() {
			@Override
			public void run() {
				uploadImage(hash, files, ix);
			}
		}.schedule(3000);
		return null;
	}

	private static class ImgLoadState {
		public boolean loaded;
		public int failCount;
	}

	private void _putImage(String hash, FileList files, int ix) {
		GWT.log("_putImage: " + files.getAt(ix).name);
		IpfsGatewayEntry putGw = new IpfsGateway().getWritable();
		File file = files.getAt(ix);
		String prefix = zeroPadded((int) files.length, ix);
		String zeroPaddedName = prefix + "-" + file.name;
		String encodedName = URL.encode(zeroPaddedName);
		String xhrUrl = putGw.getBaseUrl().replace(":hash", hash) + "/" + encodedName;
		String size = NumberFormat.getDecimalFormat().format(Math.ceil(file.size / Consts.KB)) + " KB";

		view.setFileText(file.name + " (" + size + ") [" + (ix + 1) + " of " + files.length + "]");
		GWT.log("PUT: " + xhrUrl);
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("PUT", xhrUrl, true);
		xhr.onloadend = (e) -> onImagePutDone(hash, files, ix, zeroPaddedName,
				xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH), xhr.status);
		xhr.upload.onprogress = this::updateProgressView;
		xhr.send(file.slice());
	}

	public void onImagePutDone(String hash, FileList files, int ix, String zeroPaddedName, String newHash,
			double xhrStatus) {
		String encodedName = URL.encode(zeroPaddedName);
		view.setProgressIndeterminate();
		if (xhrStatus != 201) {
			view.setProgress(0);
			GWT.log("PUT failed.");
			retryPutImage(hash, files, ix);
			return;
		}
		// the first successful GET becomes the assigned URL
		final HTMLImageElement[] imgs = new HTMLImageElement[4];
		Set<String> already = new HashSet<>();
		ImgLoadState state = new ImgLoadState();
		state.failCount = 0;
		state.loaded = false;
		int imgsLength = imgs.length;
		for (int iy = 0; iy < imgsLength; iy++) {
			IpfsGatewayEntry fetchGw = new IpfsGateway().getAnyReadonly();
			String picUrl = fetchGw.getBaseUrl().replace(":hash", newHash) + "/" + encodedName;
			String thumbUrl = fetchGw.getBaseUrl().replace(":hash", newHash) + "/thumb/" + encodedName;
			if (already.contains(thumbUrl)) {
				continue;
			}
			already.add(thumbUrl);
			final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
			imgs[iy] = img;
			img.onabort = (e2) -> onTestLoadImageAbort(hash, files, ix, state, imgsLength, fetchGw, img);
			img.onerror = (e2) -> onTestLoadImageError(hash, files, ix, state, imgsLength, fetchGw, img);
			img.onload = (e2) -> onTestLoadImage(files, ix, newHash, imgs, state, fetchGw, picUrl, thumbUrl);
			img.setAttribute("src", thumbUrl);
		}
	}

	public void updateProgressView(ProgressEvent p0) {
		if (p0.lengthComputable) {
			view.setProgress(Math.floor(p0.loaded * 100 / p0.total));
		} else {
			view.setProgressIndeterminate();
		}
	}

	public Object onTestLoadImageAbort(String hash, FileList files, int ix, ImgLoadState state, int imgsLength,
			IpfsGatewayEntry fetchGw, final HTMLImageElement img) {
		if (state.loaded) {
			return null;
		}
		img.onload = (e) -> null;
		GWT.log("IMG GET FAILED (abort): " + img.src);
		fetchGw.fail();
		state.failCount++;
		if (state.failCount >= imgsLength) {
			Scheduler.get().scheduleDeferred(ipfsGwCache::forceExpireGateways);
			retryPutImage(hash, files, ix);
		}
		return null;
	}

	public String zeroPadded(int length, int ix) {
		ix++;
		int zeroCount = String.valueOf((int) length).length();
		int digitCount = String.valueOf((int) ix).length();
		return StringUtils.repeat("0", zeroCount - digitCount) + String.valueOf((int) ix);
	}

	public Object onTestLoadImageError(String hash, FileList files, int ix, ImgLoadState state, int imgsLength,
			IpfsGatewayEntry fetchGw, final HTMLImageElement img) {
		if (state.loaded) {
			return null;
		}
		GWT.log("IMG GET FAILED (error): " + img.src);
		fetchGw.fail();
		state.failCount++;
		if (state.failCount >= imgsLength) {
			Scheduler.get().scheduleDeferred(ipfsGwCache::forceExpireGateways);
			retryPutImage(hash, files, ix);
		}
		return null;
	}

	public Object onTestLoadImage(FileList files, int ix, String hash, final HTMLImageElement[] imgs,
			ImgLoadState state, IpfsGatewayEntry fetchGw, String picUrl, String thumbUrl) {
		if (state.loaded) {
			return null;
		}
		state.loaded = true;
		for (HTMLImageElement i : imgs) {
			if (i == null) {
				continue;
			}
			i.removeAttribute("src");
			i.removeAttribute("srcset");
		}
		last = fetchGw;
		fetchGw.resetFail();
		pics.add(new ImageUrl(thumbUrl, picUrl));
		Scheduler.get().scheduleDeferred(() -> updatePreview());
		Scheduler.get().scheduleDeferred(() -> uploadImage(hash, files, ix + 1));
		return null;
	}

	@Override
	public void setView(View view) {
		this.view = view;
	}

	@Override
	public void wantsNsfw(Boolean value) {
		this.nsfw=value;
	}

	@Override
	public void postGallery() {
		if (StringUtils.isBlank(wif)){
			view.alert("Your steemit private posting key is required to post.");
			return;
		}
		if (StringUtils.isBlank(username)){
			view.alert("Your steemit username is required to post.");
			return;
		}
		if (pics.isEmpty()) {
			view.alert("You haven't uploaded any photos yet!");
			return;
		}
	}

	@Override
	public void updateWif(String wif) {
		this.wif=wif;
	}

	@Override
	public void updateUsername(String username) {
		this.username=username;
	}
}
