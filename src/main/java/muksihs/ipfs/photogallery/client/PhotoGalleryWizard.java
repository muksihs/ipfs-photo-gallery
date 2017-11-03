package muksihs.ipfs.photogallery.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Clear;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window.Location;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import gwt.material.design.client.ui.MaterialLoader;
import muksihs.ipfs.photogallery.client.ViewHandler.ShowView;
import muksihs.ipfs.photogallery.client.ViewHandler.View;
import muksihs.ipfs.photogallery.shared.Consts;
import muksihs.ipfs.photogallery.shared.GalleryInfo;
import muksihs.ipfs.photogallery.shared.ImageData;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.ui.GlobalEventBus;
import steem.CommentMetadata;
import steem.CommentResult;
import steem.SteemBroadcast;
import steem.SteemBroadcast.Beneficiary;
import steem.SteemBroadcast.CommentOptionsExtensions;
import steem.SteemCallback;
import steem.SteemError;

public class PhotoGalleryWizard implements ScheduledCommand, GlobalEventBus {

	private static final String STYLE_PULL_LEFT = "float: left; padding-left: 1rem; max-width: 50%;";

	private static final String STYLE_PULL_RIGHT = "float: right; padding-left: 1rem; max-width: 50%;";

	private static final String ATTR_STYLE = "style";

	private static final String STEEMIT_PULL_LEFT = "pull-left";

	private static final String STEEMIT_PULL_RIGHT = "pull-right";

	interface MyEventBinder extends EventBinder<PhotoGalleryWizard> {
	}

	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	private static final Set<String> whitelist = new HashSet<>(Arrays.asList( //
			"h1", "h2", "h3", //
			"h4", "h5", "h6", "a", "div", "span", //
			"p", "img", "ol", "ul", "li", "table", //
			"tr", "td", "thead", "center", "strong", //
			"b", "em", "i", "strike", "u", "cite", //
			"blockquote", "pre", "br", "hr"));

	public static native JsArray<Node> getAttributes(Element elem)/*-{
																	return elem.attributes;
																	}-*/;

	private List<ImageData> imageDataList = new ArrayList<>();

	private GalleryInfo galleryInfo;

	private String author;

	private String permLink;

	private String category;

	public PhotoGalleryWizard() {
		eventBinder.bindEventHandlers(this, eventBus);
		IpfsGatewayCache.get();
		new ViewHandler();
	}

	public void addImage(Element parent, Iterator<ImageData> iter) {
		if (!iter.hasNext()) {
			Element i = DOM.createImg();
			i.setAttribute("src", "https://ipfs.io/ipfs/" + Consts.PLACEHOLDER);
			parent.appendChild(i);
			return;
		}
		ImageData image;
		image = iter.next();
		if (image != null) {
			Element a = DOM.createAnchor();
			Element i = DOM.createImg();
			a.appendChild(i);
			a.setAttribute("href", image.getImageUrl());
			a.setAttribute("target", "_blank");
			i.setAttribute("src", image.getThumbUrl());
			parent.appendChild(a);
		} else {
			Element i = DOM.createImg();
			i.setAttribute("src", "https://ipfs.io/ipfs/" + Consts.PLACEHOLDER);
			parent.appendChild(i);
		}
	}

	@EventHandler
	protected void addImages(Event.AddImages event) {
		if (event.getFiles() == null || event.getFiles().length == 0) {
			fireEvent(new Event.EnableSelectImages(true));
			return;
		}
		fireEvent(new Event.EnableSelectImages(false));
		new LoadFileImages().load(event.getFiles());
	}

	@EventHandler
	protected void addImagesDone(Event.AddImagesDone event) {
		fireEvent(new Event.EnableSelectImages(true));
	}

	@EventHandler
	protected void doPostGallery(Event.PostGallery event) {
		DomGlobal.console.log("doPostGallery");
		String userName = event.getUserName();
		userName = userName.trim();
		while (userName.startsWith("@")) {
			userName = userName.substring(1).trim();
		}
		final CommentMetadata metadata;
		final String body;
		final String title;
		final String permLink;
		final String author;
		final String firstTag;
		final String parentAuthor;
		final String wif;
		metadata = new CommentMetadata();
		try {
			metadata.setApp("MuksihsPhotoGalleryMaker/1.0");
			metadata.setFormat("html");
			metadata.setTags(galleryInfo.getTags().toArray(new String[0]));
			Element wrapper = DOM.createDiv();
			wrapper.appendChild(getGalleryHtml4());
			body = wrapper.getInnerHTML();
			title = galleryInfo.getTitle();
			String tmp = galleryInfo.getTitle().toLowerCase().replaceAll("[^a-z0-9]", "-");
			while (tmp.endsWith("-")) {
				tmp = tmp.substring(0, tmp.length() - 1);
			}
			tmp = tmp.toLowerCase().replaceAll("-+", "-") + "-"
					+ new java.sql.Date(System.currentTimeMillis()).toString() + "-" + System.currentTimeMillis();
			while (tmp.startsWith("-")) {
				tmp = tmp.substring(1);
			}
			permLink = tmp;
			author = userName;
			firstTag = galleryInfo.getTags().iterator().next();
			parentAuthor = "";
			wif = event.getPostingKey();
			GWT.log("Posting key: '" + wif + "'");
		} catch (Exception e1) {
			GWT.log(e1.getMessage(), e1);
			return;
		}

		SteemCallback<CommentResult> doneCallback = new SteemCallback<CommentResult>() {
			@Override
			public void onResult(SteemError error, CommentResult result) {
				MaterialLoader.loading(false);
				if (error != null) {
					GWT.log("ERROR: " + error);
					fireEvent(new Event.AlertMessage("ERROR!", error.getMessage()));
					fireEvent(new Event.PostGalleryDone(author, firstTag, permLink));
				}
				if (result != null) {
					GWT.log("RESULT: " + result);
					fireEvent(new Event.PostGalleryDone(author, firstTag, permLink));
				}
			}
		};

		SteemCallback<CommentResult> callback = new SteemCallback<CommentResult>() {
			@Override
			public void onResult(SteemError error, CommentResult result) {
				MaterialLoader.loading(false);
				if (error != null) {
					GWT.log("ERROR: " + error);
				}
				if (result != null) {
					GWT.log("RESULT: " + result);
					MaterialLoader.loading(true);
					CommentOptionsExtensions extensions = new CommentOptionsExtensions();
					extensions.beneficiaries.beneficiaries.add(new Beneficiary("muksihs", event.getTipAmount()));
					SteemBroadcast.commentOptions(wif, author, permLink, extensions, doneCallback);
				}
			}
		};

		try {
			DomGlobal.console.log("SteemBroadcast.comment");
			MaterialLoader.loading(true);
			SteemBroadcast.comment(wif, parentAuthor, firstTag, author, permLink, title, body, metadata, callback);
		} catch (Exception e) {
			MaterialLoader.loading(false);
			GWT.log(e.getMessage(), e);
		}
	}

	@Override
	public void execute() {
		fireEvent(new Event.AppLoaded());
		fireEvent(new ShowView(View.Loading));
		Scheduler.get().scheduleFixedDelay(() -> {
			boolean ready = IpfsGateway.isReady();
			if (ready) {
				fireEvent(new Event.IpfsGatewayReady());
				fireEvent(new ShowView(View.SelectImages));
			}
			return !ready;
		}, 250);
	}

	@EventHandler
	protected void onPostGalleryDone(Event.PostGalleryDone event) {
		this.author = event.getAuthor();
		this.category = event.getCategory();
		this.permLink = event.getPermLink();
		fireEvent(new ShowView(View.ViewPost));
	}

	@EventHandler
	protected void getLinkInfo(Event.GetViewLinkInfo event) {
		fireEvent(new Event.LinkInfo(author, category, permLink));
	}

	@EventHandler
	protected void getAppVersion(Event.GetAppVersion event) {
		fireEvent(new Event.DisplayAppVersion("20171103"));
	}

	public Element getGalleryHtml4() {
		Element gallery = DOM.createDiv();
		Element hr = DOM.createElement("hr");
		hr.getStyle().setClear(Clear.BOTH);
		try {
			gallery.appendChild(useDeprecatedHtml4Tags(galleryInfo.getDescription()));
			gallery.appendChild(hr.cloneNode(true));
			Iterator<ImageData> iter = imageDataList.iterator();
			Element imageDiv = DOM.createDiv();
			gallery.appendChild(imageDiv);
			while (iter.hasNext()) {
				Element row = DOM.createDiv();
				row.getStyle().setClear(Clear.BOTH);
				row.addClassName("image-row");
				imageDiv.appendChild(row);

				Element col1 = DOM.createDiv();
				markPullLeft(col1);
				row.appendChild(col1);

				Element pic1 = DOM.createDiv();
				markPullLeft(pic1);
				addImage(pic1, iter);
				col1.appendChild(pic1);

				Element pic2 = DOM.createDiv();
				markPullRight(pic2);
				addImage(pic2, iter);
				col1.appendChild(pic2);

				Element col2 = DOM.createDiv();
				markPullRight(col2);
				row.appendChild(col2);

				Element pic3 = DOM.createDiv();
				markPullLeft(pic3);
				addImage(pic3, iter);
				col2.appendChild(pic3);

				Element pic4 = DOM.createDiv();
				markPullRight(pic4);
				addImage(pic4, iter);
				col2.appendChild(pic4);
			}
			gallery.appendChild(hr.cloneNode(true));
			/*
			 * add link HTML, this method auto escapes the URL if needed. yeah.. this is
			 * hacky... but does generate correct HTML structure automatically!
			 */
			Element span1 = DOM.createSpan();
			span1.setInnerText("Post your own photo gallery!");
			Element br = DOM.createElement("br");
			Element span2 = DOM.createSpan();
			span2.setInnerText("Muksih's Photo Gallery Maker");
			Element a = DOM.createAnchor();
			a.setAttribute("href", Location.getHref());
			a.appendChild(span2);
			Element pullRight = DOM.createDiv();
			markPullRight(pullRight);
			pullRight.appendChild(span1);
			pullRight.appendChild(br);
			pullRight.appendChild(a);
			Element linkHtml = DOM.createDiv();
			linkHtml.appendChild(pullRight);
			gallery.appendChild(linkHtml);
			gallery.appendChild(hr.cloneNode(true));
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
		return gallery;
	}

	private void markPullRight(Element element) {
		element.addClassName(STEEMIT_PULL_RIGHT);
		element.setAttribute(ATTR_STYLE, STYLE_PULL_RIGHT);
	}

	private void markPullLeft(Element element) {
		element.addClassName(STEEMIT_PULL_LEFT);
		element.setAttribute(ATTR_STYLE, STYLE_PULL_LEFT);
	}

	@EventHandler
	protected void imageDataAdded(Event.ImageDataAdded event) {
		imageDataList.add(event.getData());
		fireEvent(new Event.AddToPreviewPanel(event.getData()));
		fireEvent(new Event.UpdateImageCount(imageDataList.size()));
	}

	@EventHandler
	protected void removeImageFromList(Event.RemoveImage event) {
		if (event.getIndex() < 0 || event.getIndex() >= imageDataList.size()) {
			return;
		}
		imageDataList.remove(event.getIndex());
		fireEvent(new Event.UpdateImageCount(imageDataList.size()));
	}

	@EventHandler
	protected void selectImagesNext(Event.SelectImagesNext event) {
		fireEvent(new ShowView(View.UploadImages));
		Scheduler.get().scheduleDeferred(new StoreImagesInIpfs(imageDataList));
	}

	@EventHandler
	protected void setGalleryInfo(Event.GalleryInfo event) {
		GWT.log("setGalleryInfo");
		this.galleryInfo = event.getGalleryInfo();
		fireEvent(new ShowView(View.PostGallery));
	}

	@EventHandler
	protected void setGalleryInfoNext(Event.SetGalleryInfoNext event) {
		GWT.log("setGalleryInfoNext");
		fireEvent(new Event.GetGalleryInfo());
	}

	@EventHandler
	protected void storeImagesDone(Event.StoreImagesDone event) {
		fireEvent(new ShowView(View.SetGalleryInfo));
	}

	private void updatePostPreview() {
		GWT.log("updatePostPreview");
		Element galleryHtml = getGalleryHtml4();
		fireEvent(new Event.SetPreviewTitle(galleryInfo.getTitle()));
		fireEvent(new Event.SetPreviewHtml(galleryHtml));
	}

	private void useDeprecatedHtml4Tags(Element node) {
		if (node.getNodeType() == Node.TEXT_NODE) {
			return;
		}
		String tag = String.valueOf(node.getNodeName()).toLowerCase();
		if (!whitelist.contains(tag)) {
			GWT.log("REMOVED: " + tag);
			node.removeFromParent();
			return;
		}

		// walk list in reverse to simplify deletes
		JsArray<Node> attrs = getAttributes(node);
		attrs: for (int iy = attrs.length() - 1; iy >= 0; iy--) {
			Node attr = attrs.get(iy);
			String aname = attr.getNodeName();
			if (aname.equals("src")) {
				continue;
			}
			if (aname.equals("srcset")) {
				continue;
			}
			if (aname.equals("target")) {
				continue;
			}
			if (aname.equals("href")) {
				continue;
			}
			if (aname.equals("class")) {
				continue;
			}
			if (aname.equals(ATTR_STYLE)) {
				String style = node.getAttribute(ATTR_STYLE).toLowerCase().replaceAll("\\s+", " ");
				node.removeAttribute(ATTR_STYLE);
				List<String> styles = new ArrayList<>(Arrays.asList(style.split("\\s*;\\s*")));
				Iterator<String> iStyles = styles.iterator();
				style: while (iStyles.hasNext()) {
					String next = iStyles.next();
					if (next.equals("text-align: center")) {
						wrapChildrenIn("center", node);
						continue style;
					}
					if (next.equals("font-weight: bold")) {
						wrapChildrenIn("strong", node);
						continue style;
					}
					if (next.equals("font-style: italic")) {
						wrapChildrenIn("em", node);
						continue style;
					}
					if (next.equals("text-decoration: underline")) {
						wrapChildrenIn("u", node);
						continue style;
					}
					if (next.equals("text-decoration: line-through")) {
						wrapChildrenIn("strike", node);
						continue style;
					}
				}
				node.removeAttribute(ATTR_STYLE);
			}
			continue attrs;
		}
		if (node.hasChildNodes()) {
			NodeList<Node> children = node.getChildNodes();
			for (int ix = children.getLength() - 1; ix >= 0; ix--) {
				Node item = children.getItem(ix);
				if (item instanceof Element) {
					useDeprecatedHtml4Tags((Element) item);
				}
			}
		}
	}

	private Element useDeprecatedHtml4Tags(String htmlText) {
		Element html = DOM.createDiv();
		html.setInnerHTML(htmlText);
		try {
			useDeprecatedHtml4Tags(html);
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
		return html;
	}

	@EventHandler
	protected void wantsHtmlDisplayed(Event.WantsHtmlDisplayed event) {
		GWT.log("Event.WantsHtmlDisplayed");
		updatePostPreview();
	}

	public void wrapChildrenIn(String tagName, Element node) {
		Element e = node.getOwnerDocument().createElement(tagName);
		NodeList<Node> children = node.getChildNodes();
		for (int iz = children.getLength() - 1; iz >= 0; iz--) {
			e.appendChild(children.getItem(iz));
		}
		node.appendChild(e);
	}
}
