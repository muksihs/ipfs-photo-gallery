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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import muksihs.ipfs.photogallery.client.ViewHandler.ShowView;
import muksihs.ipfs.photogallery.client.ViewHandler.View;
import muksihs.ipfs.photogallery.shared.GalleryInfo;
import muksihs.ipfs.photogallery.shared.ImageData;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.ui.GlobalEventBus;
import steem.CommentMetadata;
import steem.CommentResult;
import steem.SteemBroadcast;
import steem.SteemCallback;
import steem.SteemError;

public class PhotoGalleryWizard implements ScheduledCommand, GlobalEventBus {

	interface MyEventBinder extends EventBinder<PhotoGalleryWizard> {
	}

	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	private List<ImageData> imageDataList = new ArrayList<>();

	private GalleryInfo galleryInfo;

	public PhotoGalleryWizard() {
		eventBinder.bindEventHandlers(this, eventBus);
		IpfsGatewayCache.get();
		new ViewHandler();
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
	protected void wantsHtmlDisplayed(Event.WantsHtmlDisplayed event) {
		GWT.log("Event.WantsHtmlDisplayed");
		updatePostPreview();
	}

	private void updatePostPreview() {
		GWT.log("updatePostPreview");
		String htmlString = getGalleryHtml4();
		fireEvent(new Event.SetPreviewTitle(galleryInfo.getTitle()));
		fireEvent(new Event.SetPreviewHtml(htmlString));
	}

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
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
		return sb.toSafeHtml().asString();
	}

	public void addImage(SafeHtmlBuilder sb, Iterator<ImageData> iter) {
		if (!iter.hasNext()) {
			return;
		}
		ImageData image;
		image = iter.next();
		if (image!=null) {
			Anchor a = new Anchor();
			a.setHref(image.getImageUrl());
			Image i = new Image();
			i.setUrl(image.getThumbUrl());
			a.getElement().appendChild(i.getElement());
			sb.appendHtmlConstant(a.getHTML());
		}
	}

	@EventHandler
	protected void doPostGallery(Event.PostGallery event) {
		DomGlobal.console.log("doPostGallery");
		SteemCallback<CommentResult> callback = new SteemCallback<CommentResult>() {
			@Override
			public void onResult(SteemError error, CommentResult result) {
				if (error != null) {
					DomGlobal.console.log("ERROR: ", error);
				}
				if (result != null) {
					DomGlobal.console.log("RESULT: ", result);
				}
			}
		};
		String userName = event.getUserName();
		userName = userName.trim();
		while (userName.startsWith("@")) {
			userName = userName.substring(1).trim();
		}
		CommentMetadata metadata;
		String body="";
		String title="";
		String permLink="";
		String author="";
		String parentPermLink="";
		String parentAuthor="";
		String wif="";
		metadata = new CommentMetadata();
		try {
			metadata.setApp("MuksihsPhotoGalleryMaker/1.0");
			metadata.setFormat("html");
			metadata.setTags(galleryInfo.getTags().toArray(new String[0]));
			body = getGalleryHtml4();
			title = galleryInfo.getTitle();
			permLink = System.currentTimeMillis() + "-" + galleryInfo.getTitle().replaceAll("[^a-zA-Z0-9]", "-");
			author = userName;
			parentPermLink = galleryInfo.getTags().iterator().next();
			parentAuthor = event.getUserName();
			wif = event.getPostingKey();
		} catch (Exception e1) {
			GWT.log(e1.getMessage(), e1);
			DomGlobal.console.log(e1);
		}

		try {
			DomGlobal.console.log("SteemBroadcast.comment");
			SteemBroadcast.comment(wif, parentAuthor, parentPermLink, author, permLink, title, body, metadata, callback);
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
			DomGlobal.console.log(e);
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

	private static final Set<String> whitelist = new HashSet<>(Arrays.asList( //
			"h1", "h2", "h3", //
			"h4", "h5", "h6", "a", "div", "span", //
			"p", "img", "ol", "ul", "li", "table", //
			"tr", "td", "thead", "center", "strong", //
			"b", "em", "i", "strike", "u", "cite", //
			"blockquote", "pre"));

	public static native JsArray<Node> getAttributes(Element elem)/*-{
																	return elem.attributes;
																	}-*/;

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
			if (aname.equals("style")) {
				String style = node.getAttribute("style").toLowerCase().replaceAll("\\s+", " ");
				node.removeAttribute("style");
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
				node.removeAttribute("style");
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

	public void wrapChildrenIn(String tagName, Element node) {
		Element e = node.getOwnerDocument().createElement(tagName);
		NodeList<Node> children = node.getChildNodes();
		for (int iz = children.getLength() - 1; iz >= 0; iz--) {
			e.appendChild(children.getItem(iz));
		}
		node.appendChild(e);
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
	protected void getAppVersion(Event.GetAppVersion event) {
		fireEvent(new Event.DisplayAppVersion("20171101"));
	}

	@EventHandler
	protected void imageDataAdded(Event.ImageDataAdded event) {
		imageDataList.add(event.getData());
		fireEvent(new Event.AddToPreviewPanel(event.getData()));
		fireEvent(new Event.UpdateImageCount(imageDataList.size()));
	}

	@EventHandler
	protected void storeImagesDone(Event.StoreImagesDone event) {
		fireEvent(new ShowView(View.SetGalleryInfo));
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
}
