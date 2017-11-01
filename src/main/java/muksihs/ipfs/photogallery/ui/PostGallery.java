package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;

import gwt.material.design.client.ui.MaterialIntegerBox;

public class PostGallery extends EventBusComposite {
	
	@UiField
	protected MaterialIntegerBox tipAmount;

	private static PostGalleryUiBinder uiBinder = GWT.create(PostGalleryUiBinder.class);

	interface PostGalleryUiBinder extends UiBinder<Widget, PostGallery> {
	}

	public PostGallery() {
		initWidget(uiBinder.createAndBindUi(this));
		tipAmount.setMin("0");
		tipAmount.setMax("100");
	}

	interface MyEventBinder extends EventBinder<PostGallery>{}
	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

}
