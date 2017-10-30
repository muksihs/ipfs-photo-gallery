package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.constants.ProgressType;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialProgress;
import muksihs.ipfs.photogallery.client.Event;

public class UploadImages extends EventBusComposite {

	private static UploadImagesUiBinder uiBinder = GWT.create(UploadImagesUiBinder.class);

	interface UploadImagesUiBinder extends UiBinder<Widget, UploadImages> {
	}
	
	@UiField
	protected MaterialPanel previewPanel;
	
	@UiField
	protected MaterialProgress progress;

	public UploadImages() {
		initWidget(uiBinder.createAndBindUi(this));
		progress.setType(ProgressType.DETERMINATE);
		progress.setPercent(0);
		progress.setTitle("0%");
	}
	
	@EventHandler
	protected void updatePreviewPanel(Event.AddToPreviewPanel event) {
		MaterialImage image = new MaterialImage(event.getImageDataUrl());
		image.setCaption(event.getCaption());
		image.setTitle(event.getCaption());
		previewPanel.add(image);
	}
	
	interface MyEventBinder extends EventBinder<UploadImages>{}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

}
