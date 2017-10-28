package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialLabel;
import muksihs.ipfs.photogallery.client.Event;

public class HeaderBlock extends EventBusComposite {
	
	interface MyEventBinder extends EventBinder<HeaderBlock> {
	}
	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}
	
	private static String versionTxt="19000101";
	
	@UiField
	protected MaterialLabel version;

	private static HeaderBlockUiBinder uiBinder = GWT.create(HeaderBlockUiBinder.class);

	interface HeaderBlockUiBinder extends UiBinder<Widget, HeaderBlock> {
	}

	public HeaderBlock() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		version.setText(versionTxt);
	}
	
	@EventHandler
	public void setAppVersion(Event.DisplayAppVersion event) {
		GWT.log("APP VERSION: "+event.getVersion());
		version.setText(event.getVersion());
		versionTxt=event.getVersion();
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		fireEvent(new Event.GetAppVersion());
	}
	
	@Override
	protected void onUnload() {
		super.onUnload();
	}
}
