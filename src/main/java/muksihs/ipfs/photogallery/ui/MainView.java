package muksihs.ipfs.photogallery.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element.OnabortCallbackFn;
import elemental2.dom.Event;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLImageElement.OnerrorCallbackFn;
import elemental2.dom.HTMLImageElement.OnloadCallbackFn;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequest;
import elemental2.dom.XMLHttpRequest.OnloadendCallbackFn;
import elemental2.dom.XMLHttpRequestUpload;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialRadioButton;
import muksihs.ipfs.photogallery.client.PostingTemplates;
import muksihs.ipfs.photogallery.shared.Ipfs;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.shared.IpfsGatewayEntry;

public class MainView extends Composite {

	interface MainViewUiBinder extends UiBinder<Widget, MainView> {
	}

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	private List<String> pics = new ArrayList<>();

	@UiField
	MaterialRadioButton rb1;

	@UiField
	MaterialRadioButton rb2;;

	@UiField
	MaterialRadioButton rb4;
	// @UiField
	// MaterialRadioButton rb8;

	@UiField
	FileUpload add;

	@UiField
	MaterialButton clear;
	@UiField
	MaterialButton edit;
	@UiField
	MaterialButton showSteemitText;

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
				putImage(Ipfs.EMPTY_DIR, files, 0);
			});
		});
		Scheduler.get().scheduleDeferred(() -> {
			rb1.addClickHandler((e) -> setColumns(1));
			rb2.addClickHandler((e) -> setColumns(2));
			rb4.addClickHandler((e) -> setColumns(4));
			showSteemitText.addClickHandler((e)->showSteemitHtml());
		});
	}

	@UiField
	TextArea steemitText;
	
	private void showSteemitHtml() {
		String text = "<html>\n"+picPreview.getInnerHTML()+"\n</html>";
		text = text.replace("\t", "  ");
		text = text.replaceAll("\n+", "\n");
		steemitText.setText(text);
	}

	int maxRetries = 5;
	int retries = maxRetries;;

	private void putImage(String hash, FileList files, int ix) {
		IpfsGatewayEntry putGw = new IpfsGateway().getWritable();
		IpfsGatewayEntry fetchGw = new IpfsGateway().getAny();
		if (ix > files.length) {
			GWT.log("DONE PUTTING IMAGES.");
			String finalUrl = fetchGw.getBaseUrl().replace(":hash", hash) + "/'";
			RootPanel.get().add(new HTML("<div style='clear: both;'><a target=_blank href='" + finalUrl + ">"
					+ SafeHtmlUtils.htmlEscape(finalUrl) + "</a></div>"));
			return;
		}
		File f = files.getAt(ix);

		XMLHttpRequest xhr = new XMLHttpRequest();
		String url = putGw.getBaseUrl().replace(":hash", hash) + "/" + URL.encode(f.name);
		GWT.log("PUT: " + url);
		OnloadendCallbackFn onloadend = new OnloadendCallbackFn() {
			@Override
			public void onInvoke(ProgressEvent p0) {
				if (xhr.status != 201) {
					putGw.fail();
					putImage(hash, files, ix);
					return;
				}
				String newHash = xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH);
				HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
				String picUrl = fetchGw.getBaseUrl().replace(":hash", newHash) + "/"
						+ URL.encode(f.name);
				img.lowSrc = picUrl;
				img.src = picUrl;
				img.onabort = new OnabortCallbackFn() {
					@Override
					public Object onInvoke(Event p0) {
						fetchGw.fail();
						GWT.log("IMG GET FAILED (abort): " + img.src);
						if (retries-- > 0) {
							Scheduler.get().scheduleDeferred(() -> putImage(hash, files, ix));
						} else {
							retries = maxRetries;
							Scheduler.get().scheduleDeferred(() -> putImage(newHash, files, ix + 1));
						}
						return null;
					}
				};
				img.onerror = new OnerrorCallbackFn() {
					@Override
					public Object onInvoke(Event p0) {
						fetchGw.fail();
						GWT.log("IMG GET FAILED (error): " + img.src);
						if (retries-- > 0) {
							Scheduler.get().scheduleDeferred(() -> putImage(hash, files, ix));
						} else {
							retries = maxRetries;
							Scheduler.get().scheduleDeferred(() -> putImage(newHash, files, ix + 1));
						}
						return null;
					}
				};
				img.onload = new OnloadCallbackFn() {
					@Override
					public Object onInvoke(Event p0) {
						fetchGw.resetFail();
						pics.add(picUrl);
						retries = maxRetries;
						Scheduler.get().scheduleDeferred(() -> updatePreview());
						Scheduler.get().scheduleDeferred(() -> putImage(newHash, files, ix + 1));
						return null;
					}
				};
			}
		};
		xhr.onloadend = onloadend;

		xhr.upload.onprogress = new XMLHttpRequestUpload.OnprogressCallbackFn() {
			@Override
			public void onInvoke(ProgressEvent p0) {
				// GWT.log("onloadend#state: "+xhr.readyState+", "+xhr.status);
				// GWT.log("upload#onprogress: " + p0.eventPhase + ", " +
				// p0.loaded + "," + p0.total + ", " + p0.type);
				// GWT.log("upload#response headers:
				// "+xhr.getAllResponseHeaders());
				// GWT.log("upload#ipfs hash header:
				// "+xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH));
				// GWT.log("upload#response URL: "+xhr.responseURL);
			}
		};
		xhr.open("PUT", url, true);
		xhr.send(f.slice());
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
			tmp = tmp.replace("_" + cell + "_", pic);
		}
		if (cell <= perRow) {
			while (cell <= perRow) {
				cell++;
				tmp = tmp.replace("_" + cell + "_",
						"https://ipfs.work/ipfs/QmXdYDa885mhijwd4Jwrnr1oX9H2M5WxxHmRsjvERhaMY4/DSC00021.JPG");
			}
			previewHtml.append(tmp);
		}
		picPreview.setInnerHTML("<div>" + previewHtml.toString() + "</div>");
		// GWT.log(previewHtml.toString());
	}
}
