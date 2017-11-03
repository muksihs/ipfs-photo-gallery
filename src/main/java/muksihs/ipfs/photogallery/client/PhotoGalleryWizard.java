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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import gwt.material.design.client.ui.MaterialLoader;
import gwt.material.design.client.ui.html.Span;
import muksihs.ipfs.photogallery.client.ViewHandler.ShowView;
import muksihs.ipfs.photogallery.client.ViewHandler.View;
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

	public PhotoGalleryWizard() {
		eventBinder.bindEventHandlers(this, eventBus);
		IpfsGatewayCache.get();
		new ViewHandler();
	}

	public void addImage(SafeHtmlBuilder sb, Iterator<ImageData> iter) {
		if (!iter.hasNext()) {
			return;
		}
		ImageData image;
		image = iter.next();
		if (image != null) {
			HTMLPanel p = new HTMLPanel("");
			Anchor a = new Anchor();
			Image i = new Image();
			p.getElement().appendChild(a.getElement());
			a.getElement().appendChild(i.getElement());
			a.setHref(image.getImageUrl());
			a.setTarget("_blank");
			i.setUrl(image.getThumbUrl());
			sb.appendHtmlConstant(p.getElement().getInnerHTML());
		} else {
			sb.appendHtmlConstant(
					"<img src='https://ipfs.io/ipfs/QmQ4keX7r9YnoARDgq4YJBqRwABcfXsnnE8EkD5EnjtLVH/placeholder.png'/>");
		}
	}

	@EventHandler
	protected void addImages(Event.AddImages event) {
		if (event.getFiles() == null || event.getFiles().length == 0) {
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
		final String parentPermLink;
		final String parentAuthor;
		final String wif;
		metadata = new CommentMetadata();
		try {
			metadata.setApp("MuksihsPhotoGalleryMaker/1.0");
			metadata.setFormat("html");
			metadata.setTags(galleryInfo.getTags().toArray(new String[0]));
			body = getGalleryHtml4();
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
			parentPermLink = galleryInfo.getTags().iterator().next();
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
				}
				if (result != null) {
					GWT.log("RESULT: " + result);
					fireEvent(new Event.PostSuccess(author, permLink));
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
					extensions.beneficiaries.beneficiaries.add(new Beneficiary("muksihs", event.getTipAmount() * 100));
					SteemBroadcast.commentOptions(wif, author, permLink, extensions, doneCallback);
				}
			}
		};

		try {
			DomGlobal.console.log("SteemBroadcast.comment");
			MaterialLoader.loading(true);
			SteemBroadcast.comment(wif, parentAuthor, parentPermLink, author, permLink, title, body, metadata,
					callback);
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
	protected void getAppVersion(Event.GetAppVersion event) {
		fireEvent(new Event.DisplayAppVersion("20171102"));
	}

	//TODO: Use GWT DOM elements to ensure correct structure!
	public String getGalleryHtml4() {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		try {
			sb.appendHtmlConstant("<div>");
			String description = galleryInfo.getDescription();
			sb.appendHtmlConstant(useDeprecatedHtml4Tags(description));
			sb.appendHtmlConstant("</div>");
			sb.appendHtmlConstant("<hr/>");
			Iterator<ImageData> iter = imageDataList.iterator();
			sb.appendHtmlConstant("<div>");
			while (iter.hasNext()) {
				sb.appendHtmlConstant("<div class='pull-left'>");
				sb.appendHtmlConstant("<div class='pull-left'>");
				addImage(sb, iter);
				sb.appendHtmlConstant("</div>");
				sb.appendHtmlConstant("<div class='pull-right'>");
				addImage(sb, iter);
				sb.appendHtmlConstant("</div>");
				sb.appendHtmlConstant("</div>");
				sb.appendHtmlConstant("<div class='pull-right'>");
				sb.appendHtmlConstant("<div class='pull-left'>");
				addImage(sb, iter);
				sb.appendHtmlConstant("</div>");
				sb.appendHtmlConstant("<div class='pull-right'>");
				addImage(sb, iter);
				sb.appendHtmlConstant("</div>");
				sb.appendHtmlConstant("</div>");
			}
			sb.appendHtmlConstant("</div>");
			/*
			 * add link HTML, this method auto escapes the URL if needed. yeah.. this is
			 * hacky... but does generate correct HTML structure automatically!
			 */
			Span span1 = new Span("Created with ");
			Span span2 = new Span("Muksih's Photo Gallery Maker");
			Span span3 = new Span(".");
			Anchor a = new Anchor();
			a.setHref(Location.getHref());
			a.getElement().appendChild(span2.getElement());
			HTML pullRight = new HTML();
			markPullRight(pullRight.getElement());
			pullRight.getElement().appendChild(span1.getElement());
			pullRight.getElement().appendChild(a.getElement());
			pullRight.getElement().appendChild(span3.getElement());
			HTML linkHtml = new HTML();
			linkHtml.getElement().appendChild(pullRight.getElement());
			sb.appendHtmlConstant(linkHtml.getHTML());
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
		return sb.toSafeHtml().asString();
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
		String htmlString = getGalleryHtml4();
		fireEvent(new Event.SetPreviewTitle(galleryInfo.getTitle()));
		fireEvent(new Event.SetPreviewHtml(htmlString));
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

	private String useDeprecatedHtml4Tags(String htmlText) {
		HTML html = new HTML(htmlText);
		try {
			Element element = html.getElement();
			useDeprecatedHtml4Tags(element);
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
			return htmlText;
		}
		return html.getElement().getInnerHTML();
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
