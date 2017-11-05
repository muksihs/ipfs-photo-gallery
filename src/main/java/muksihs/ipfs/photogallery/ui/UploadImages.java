package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.constants.Display;
import gwt.material.design.client.constants.ProgressType;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialProgress;
import muksihs.ipfs.photogallery.client.Event;
import muksihs.ipfs.photogallery.shared.Consts;

public class UploadImages extends EventBusComposite {

	interface MyEventBinder extends EventBinder<UploadImages> {
	}

	interface UploadImagesUiBinder extends UiBinder<Widget, UploadImages> {
	}

	private static UploadImagesUiBinder uiBinder = GWT.create(UploadImagesUiBinder.class);

	@UiField
	protected MaterialPanel previewPanel;

	@UiField
	protected MaterialProgress progress;

	@UiField
	protected MaterialProgress xhrProgress;

	public UploadImages() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

	@EventHandler
	protected void setProgress(Event.SetProgress event) {
		progress.setType(ProgressType.DETERMINATE);
		progress.setPercent(Math.floor(event.getPercent()));
	}

	@EventHandler
	protected void setProgress(Event.SetXhrProgress event) {
		xhrProgress.setType(ProgressType.DETERMINATE);
		xhrProgress.setPercent(Math.floor(event.getPercent()));
	}

	@EventHandler
	protected void setProgress(Event.SetXhrProgressIndeterminate event) {
		xhrProgress.setType(ProgressType.INDETERMINATE);
	}
	
	@EventHandler
	protected void updatePreviewPanel(Event.AddToPreviewPanel event) {
		Document dom = Document.get();
		String title = event.getImageData().getName();
		String size = "["+Math.ceil(event.getImageData().getImageData().size/Consts.KB)+" KB]";
		MaterialPanel imgBox = new MaterialPanel();
		imgBox.setMargin(4);
		imgBox.setMaxWidth("24%");
		imgBox.setDisplay(Display.INLINE_BLOCK);
		imgBox.getElement().getStyle().setTextAlign(TextAlign.CENTER);
		AnchorElement a = dom.createAnchorElement();
		a.setHref(event.getImageData().getImageUrl());
		a.setTarget("_blank");
		MaterialImage image = new MaterialImage(event.getImageData().getThumbUrl());
		image.setCaption(event.getImageData().getName());
		image.setTitle(title+" "+size);
		image.setWidth("100%");
		a.appendChild(image.getElement());
		a.appendChild(dom.createBRElement());
		a.appendChild(dom.createTextNode(title));
		a.appendChild(dom.createBRElement());
		a.appendChild(dom.createTextNode(size));
		image.setHoverable(true);
		imgBox.getElement().appendChild(a);
		previewPanel.add(imgBox);	
	}

//	@EventHandler
//	protected void updatePreviewPanel(Event.AddToPreviewPanel event) {
//		FileReader reader = new FileReader();
//		reader.onloadend = (e) -> {
//			MaterialImage image = new MaterialImage(reader.result.asString());
//			image.setCaption(event.getImageData().getName());
//			image.setTitle(event.getImageData().getName());
//			previewPanel.add(image);
//			return null;
//		};
//		reader.readAsDataURL(event.getImageData().getThumbData());
//	}

}
