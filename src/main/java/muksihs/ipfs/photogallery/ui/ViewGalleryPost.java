package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialLink;
import muksihs.ipfs.photogallery.client.Event;

public class ViewGalleryPost extends EventBusComposite {
	
	@UiField
	protected MaterialButton another;
	@UiField
	protected MaterialLink steemit;
	@UiField
	protected MaterialLink chainbb;
	@UiField
	protected MaterialLink busyorg;

	private static ViewGalleryPostUiBinder uiBinder = GWT.create(ViewGalleryPostUiBinder.class);

	interface ViewGalleryPostUiBinder extends UiBinder<Widget, ViewGalleryPost> {
	}

	public ViewGalleryPost() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	interface MyEventBinder extends EventBinder<ViewGalleryPost>{};
	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}
	
	@EventHandler
	protected void linkInfo(Event.LinkInfo event) {
		another.setEnabled(true);
		another.addClickHandler((e)->Location.reload());
		steemit.setHref("https://www.steemit.com/@"+event.getAuthor()+"/"+event.getPermLink());
		steemit.setEnabled(true);
		chainbb.setHref("https://www.chainbb.com/@"+event.getAuthor()+"/"+event.getPermLink());
		chainbb.setEnabled(true);
		busyorg.setHref("https://www.busy.org/@"+event.getAuthor()+"/"+event.getPermLink());
		busyorg.setEnabled(true);
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		fireEvent(new Event.GetViewLinkInfo());
	}

}
