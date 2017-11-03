package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialIntegerBox;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialTextBox;
import gwt.material.design.client.ui.MaterialTitle;
import muksihs.ipfs.photogallery.client.Event;

public class PostGallery extends EventBusComposite {
	
	interface MyEventBinder extends EventBinder<PostGallery>{}
	interface PostGalleryUiBinder extends UiBinder<Widget, PostGallery> {
	}
	private static PostGalleryUiBinder uiBinder = GWT.create(PostGalleryUiBinder.class);
	@UiField
	protected MaterialIntegerBox tipAmount;
	@UiField
	protected MaterialTextBox username;
	@UiField
	protected MaterialTextBox postingKey;

	@UiField
	protected MaterialButton post;

	@UiField
	protected MaterialTitle previewTitle;
	
	@UiField
	protected MaterialPanel previewPanel;
	public PostGallery() {
		initWidget(uiBinder.createAndBindUi(this));
		tipAmount.setMin("0");
		tipAmount.setMax("100");
		tipAmount.setAlignment(TextAlignment.RIGHT);
		tipAmount.addBlurHandler((e)->{
			if (tipAmount.getValue()<0) {
				tipAmount.setValue(0);
			}
			if (tipAmount.getValue()>100) {
				tipAmount.setValue(100);
			}
			tipAmount.setValue(tipAmount.getValue());
		});
		post.addClickHandler((e)->{
			DomGlobal.console.log("Posting...");
			Event.PostGallery event = new Event.PostGallery(username.getValue(), postingKey.getValue(), tipAmount.getValue());
			GWT.log("Event data: "+event.toString());
			fireEvent(event);
		});
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		GWT.log("firing: Event.ViewLoaded");
		fireEvent(new Event.WantsHtmlDisplayed());
	}
	
	@EventHandler
	protected void setPreviewHtml(Event.SetPreviewHtml event) {
		previewPanel.getElement().setInnerHTML(event.getPreviewHtml());
	}
	@EventHandler
	protected void setPreviewTitle(Event.SetPreviewTitle event) {
		previewTitle.setTitle(event.getTitle());
	}

}
