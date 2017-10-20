package muksihs.ipfs.photogallery.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import elemental2.dom.Blob;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element.OnabortCallbackFn;
import elemental2.dom.Event;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.FileReader;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLCanvasElement.ToBlobCallbackFn;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLImageElement.OnerrorCallbackFn;
import elemental2.dom.HTMLImageElement.OnloadCallbackFn;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequest;
import elemental2.dom.XMLHttpRequest.OnloadendCallbackFn;
import elemental2.dom.XMLHttpRequestUpload;
import gwt.material.design.client.constants.ProgressType;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialProgress;
import gwt.material.design.client.ui.MaterialRadioButton;
import muksihs.ipfs.photogallery.client.PhotoGallery;
import muksihs.ipfs.photogallery.client.PostingTemplates;
import muksihs.ipfs.photogallery.shared.Ipfs;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.shared.IpfsGatewayEntry;

public class MainView extends Composite {

	private static final int KB = 1024;

	private static final double maxSize = 840;

	private static final double jpgQuality = 0.7;

	private static final String PLACEHOLDER_HASH = "QmQ4keX7r9YnoARDgq4YJBqRwABcfXsnnE8EkD5EnjtLVH";

	interface MainViewUiBinder extends UiBinder<Widget, MainView> {
	}

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	private List<String> pics = new ArrayList<>();

	@UiField
	MaterialLabel filename;

	@UiField
	MaterialProgress progress;

	@UiField
	MaterialRadioButton rb1;

	@UiField
	MaterialRadioButton rb2;;

	@UiField
	MaterialRadioButton rb4;

	@UiField
	FileUpload add;

	@UiField
	MaterialButton clear;
	@UiField
	MaterialButton edit;
	@UiField
	MaterialButton showSteemitText;

	@UiField
	MaterialLink link;

	@UiField
	DivElement picPreview;
	private int columns = 1;

	public MainView() {
		initWidget(uiBinder.createAndBindUi(this));
		Scheduler.get().scheduleDeferred(() -> {
			rb4.setValue(true, true);
			setColumns(4);
		});
		Scheduler.get().scheduleDeferred(() -> {
			add.getElement().setId("upload");
			add.getElement().setAttribute("multiple", "multiple");
			add.getElement().setAttribute("accept", "image/*");
			add.addChangeHandler((e) -> {
				GWT.log("file list changed");
				HTMLInputElement x = (HTMLInputElement) DomGlobal.document.getElementById("upload");
				if (x == null) {
					Window.alert("Can't find input element!");
				}
				FileList files = x.files;
				GWT.log("Have " + files.length + " files to upload.");
				link.setEnabled(false);
				add.setEnabled(false);
				putImage(Ipfs.EMPTY_DIR, files, 0);
			});
		});
		rb1.addClickHandler((e) -> setColumns(1));
		rb2.addClickHandler((e) -> setColumns(2));
		rb4.addClickHandler((e) -> setColumns(4));
		showSteemitText.addClickHandler((e) -> showSteemitHtml());
		clear.addClickHandler((e) -> Location.reload());
		edit.setVisible(false);
		edit.setEnabled(false);
		add.setEnabled(false);
		Scheduler.get().scheduleFixedDelay(() -> {
			boolean ready = IpfsGateway.isReady();
			add.setEnabled(ready);
			Scheduler.get().scheduleDeferred(() -> updatePreview());
			return !ready;
		}, 1000);
	}

	@UiField
	TextArea steemitText;

	private void showSteemitHtml() {
		String aHref = "<a href='" + GWT.getHostPageBaseURL() + "'>";
		String text = "<html>\n" + picPreview.getInnerHTML() //
				+ "\n" //
				+ "<div class='pull-right'>" //
				+ aHref + "Muksihs' IPFS Photo Gallery Maker</a>" //
				+ "<br/>@muksihs" //
				+ "</div>" //
				+ "\n</html>";
		text = text.replace("\t", "  ");
		text = text.replaceAll("\n+", "\n");
		steemitText.setText(text);
	}

	private IpfsGatewayEntry last = new IpfsGateway().getAny();

	private void putImage(String hash, FileList files, int ix) {
		if (ix >= files.length) {
			filename.setText("Upload complete.");
			progress.setType(ProgressType.DETERMINATE);
			progress.setPercent(0);
			GWT.log("DONE PUTTING IMAGES.");
			String finalUrl = last.getBaseUrl().replace(":hash", hash) + "/";
			link.setEnabled(true);
			link.setHref(finalUrl);
			link.getElement().setAttribute("target", "_blank");
			Scheduler.get().scheduleDeferred(() -> showSteemitHtml());
			add.setEnabled(true);
			return;
		}
		_putThumb(hash, files, ix);
	}

	private void _putThumb(String hash, FileList files, int ix) {
		String prefix = zeroPadded((int) files.length, ix);
		IpfsGatewayEntry putGw = new IpfsGateway().getWritable();

		File f = files.getAt(ix);
		filename.setText(f.name + " (Thumbnail) [" + (ix + 1) + " of " + files.length + "]");

		HTMLImageElement thumbImg = (HTMLImageElement) DomGlobal.document.createElement("img");
		FileReader r = new FileReader();
		r.onabort = (e2) -> retryPutImage(hash, files, ix);
		r.onerror = (e2) -> retryPutImage(hash, files, ix);
		r.onloadend = (e) -> {
			// load the image
			thumbImg.onabort = (e2) -> retryPutImage(hash, files, ix);
			thumbImg.onerror = (e2) -> retryPutImage(hash, files, ix);
			thumbImg.onload = (e2) -> {
				double scale;
				double w = thumbImg.width;
				double h = thumbImg.height;
				// reduce
				if (w > h) {
					scale = maxSize / w;
				} else {
					scale = maxSize / h;
				}
				// create thumbnail image
				HTMLCanvasElement thumb = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
				thumb.width = thumbImg.width * scale;
				thumb.height = thumbImg.height * scale;
				CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) (Object) thumb.getContext("2d");
				ctx.drawImage(thumbImg, 0, 0, thumbImg.width * scale, thumbImg.height * scale);
				String mime = thumbImg.src.startsWith("data:image/png") ? "image/png" : "image/jpeg";
				thumb.toBlob(new ToBlobCallbackFn() {
					@Override
					public Object onInvoke(Blob p0) {
						GWT.log("thumb type: " + p0.type);
						// post thumbnail image
						String baseUrl = putGw.getBaseUrl();
						String xhrUrl = baseUrl.replace(":hash", hash) + "/thumb/" + URL.encode(prefix + "-" + f.name);
						XMLHttpRequest xhr = new XMLHttpRequest();
						xhr.open("PUT", xhrUrl, true);
						GWT.log("PUT: " + xhrUrl);
						xhr.onloadend = new OnloadendCallbackFn() {
							@Override
							public void onInvoke(ProgressEvent onloadendEvent) {
								progress.setType(ProgressType.INDETERMINATE);
								if (xhr.status != 201) {
									progress.setType(ProgressType.DETERMINATE);
									progress.setPercent(0);
									GWT.log("PUT failed.");
									putGw.fail();
									PhotoGallery.resetGatewaysPingStatus();
									retryPutImage(hash, files, ix);
									return;
								}
								String newHash = xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH);
								if (newHash==null||newHash.trim().isEmpty()) {
									retryPutImage(hash, files, ix);
									return;
								}
								_putImage(newHash, files, ix);
							}
						};
						xhr.onabort = (e3) -> retryPutImage(hash, files, ix);
						xhr.send(p0);
						return p0;
					}
				}, mime, jpgQuality);
				return e2;
			};
			thumbImg.src = r.result.asString();
			return e;
		};
		r.readAsDataURL(f.slice());
	}

	public Object retryPutImage(String hash, FileList files, int ix) {
		GWT.log("retryImage: " + files.getAt(ix).name);
		new Timer() {
			@Override
			public void run() {
				putImage(hash, files, ix);
			}
		}.schedule(3000);
		return null;
	}

	private void _putImage(String hash, FileList files, int ix) {
		String prefix = zeroPadded((int) files.length, ix);

		IpfsGatewayEntry putGw = new IpfsGateway().getWritable();
		if (ix >= files.length) {
			filename.setText("");
			progress.setType(ProgressType.DETERMINATE);
			progress.setPercent(0);
			GWT.log("DONE PUTTING IMAGES.");
			String finalUrl = last.getBaseUrl().replace(":hash", hash) + "/";
			link.setEnabled(true);
			link.setHref(finalUrl);
			link.getElement().setAttribute("target", "_blank");
			Scheduler.get().scheduleDeferred(() -> showSteemitHtml());
			add.setEnabled(true);
			return;
		}

		File f = files.getAt(ix);
		String size = NumberFormat.getDecimalFormat().format(Math.ceil(f.size / KB)) + " KB";
		filename.setText(f.name + " (" + size + ") [" + (ix + 1) + " of " + files.length + "]");
		filename.setTitle(f.name + ": " + (new java.sql.Date((long) f.lastModified)).toString() + ", "
				+ Math.ceil(f.size / 1024) + " KB");
		String xhrUrl = putGw.getBaseUrl().replace(":hash", hash) + "/" + URL.encode(prefix + "-" + f.name);
		GWT.log("PUT: " + xhrUrl);
		XMLHttpRequest xhr = new XMLHttpRequest();
		xhr.open("PUT", xhrUrl, true);
		xhr.onloadend = new OnloadendCallbackFn() {
			@Override
			public void onInvoke(ProgressEvent onloadendEvent) {
				progress.setType(ProgressType.INDETERMINATE);
				if (xhr.status != 201) {
					progress.setType(ProgressType.DETERMINATE);
					progress.setPercent(0);
					GWT.log("PUT failed.");
					putGw.fail();
					retryPutImage(hash, files, ix);
					// if (xhr.status == 413) {
					// GWT.log("BLACKLISTING: " + putGw.getBaseUrl());
					// putGw.setAlive(false);
					// putGw.setExpires(System.currentTimeMillis() + 24 * 60l *
					// PhotoGallery.MINUTE);
					// PhotoGallery.cacheIpfsGatewayStatus(putGw);
					// }
					return;
				}
				String newHash = xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH);
				// the first successful GET becomes the assigned URL
				final HTMLImageElement[] imgs = new HTMLImageElement[4];
				final int[] failCount = { 0 };
				boolean[] loaded = { false };
				Set<String> already = new HashSet<>();
				for (int iy = 0; iy < imgs.length; iy++) {
					IpfsGatewayEntry fetchGw = new IpfsGateway().getAnyReadonly();
					String picUrl = fetchGw.getBaseUrl().replace(":hash", newHash) + "/"
							+ URL.encode(prefix + "-" + f.name);
					String thumbUrl = fetchGw.getBaseUrl().replace(":hash", newHash) + "/thumb/"
							+ URL.encode(prefix + "-" + f.name);
					if (already.contains(picUrl)) {
						continue;
					}
					already.add(picUrl);
					final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
					imgs[iy] = img;
					img.onabort = new OnabortCallbackFn() {
						@Override
						public Object onInvoke(Event onabortEvent) {
							if (loaded[0]) {
								return null;
							}
							img.onabort = (e) -> null;
							img.onerror = (e) -> null;
							img.onload = (e) -> null;
							GWT.log("IMG GET FAILED (abort): " + img.src);
							fetchGw.fail();
							failCount[0]++;
							if (failCount[0] >= imgs.length) {
								Scheduler.get().scheduleDeferred(() -> PhotoGallery.resetGatewaysPingStatus());
								retryPutImage(hash, files, ix);
							}
							return null;
						}
					};
					img.onerror = new OnerrorCallbackFn() {
						@Override
						public Object onInvoke(Event onerrorEvent) {
							img.onabort = (e) -> null;
							img.onerror = (e) -> null;
							img.onload = (e) -> null;
							if (loaded[0]) {
								return null;
							}
							GWT.log("IMG GET FAILED (error): " + img.src);
							fetchGw.fail();
							failCount[0]++;
							if (failCount[0] >= imgs.length) {
								Scheduler.get().scheduleDeferred(() -> PhotoGallery.resetGatewaysPingStatus());
								retryPutImage(hash, files, ix);
							}
							return null;
						}
					};
					img.onload = new OnloadCallbackFn() {
						@Override
						public Object onInvoke(Event onloadevent) {
							if (loaded[0]) {
								return null;
							}
							loaded[0] = true;
							for (HTMLImageElement i : imgs) {
								if (i == null) {
									continue;
								}
								i.onabort = (e) -> {
									GWT.log("abort: " + i.src);
									return null;
								};
								i.onerror = (e) -> {
									GWT.log("error: " + i.src);
									return null;
								};
								i.onload = (e) -> {
									GWT.log("onload: " + i.src);
									return null;
								};
								i.removeAttribute("src");
								i.removeAttribute("srcset");
							}
							last = fetchGw;
							fetchGw.resetFail();
							pics.add(picUrl);
							Scheduler.get().scheduleDeferred(() -> updatePreview());
							Scheduler.get().scheduleDeferred(() -> putImage(newHash, files, ix + 1));
							return null;
						}
					};
					img.crossOrigin = "anonymous";
					img.setAttribute("src", thumbUrl);
				}
			}
		};

		xhr.upload.onprogress = new XMLHttpRequestUpload.OnprogressCallbackFn() {
			@Override
			public void onInvoke(ProgressEvent p0) {
				if (p0.lengthComputable) {
					progress.setType(ProgressType.DETERMINATE);
					progress.setPercent(Math.floor(p0.loaded * 100 / p0.total));
				} else {
					progress.setType(ProgressType.INDETERMINATE);
				}
			}
		};
		xhr.send(f.slice());
	}

	public String zeroPadded(int length, int ix) {
		ix++;
		int zeroCount = String.valueOf((int) length).length();
		int digitCount = String.valueOf((int) ix).length();
		return StringUtils.repeat("0", zeroCount - digitCount) + String.valueOf((int) ix);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
	}

	private void setColumns(int columns) {
		this.columns = columns;
		Scheduler.get().scheduleDeferred(this::updatePreview);
		GWT.log("Columns: " + columns);
	}

	private void updatePreview() {
		final String placeholder = PLACEHOLDER_HASH + "/placeholder.png";
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
		Iterator<String> iPics = pics.iterator();
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
			String pic = iPics.next();
			// set img src and a href values.
			tmp = tmp.replace("_IMG" + cell + "_", pic);
			// set alt text values.
			String name = StringUtils.substringAfterLast(pic, "/");
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
		picPreview.setInnerHTML("<div>" + previewHtml.toString() + "</div>");
		// GWT.log(previewHtml.toString());
	}
}
