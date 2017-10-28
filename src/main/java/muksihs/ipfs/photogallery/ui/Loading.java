package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

import muksihs.ipfs.photogallery.client.Event;

public class Loading extends EventBusComposite {

	private static final LoadingUiBinder uiBinder = GWT.create(LoadingUiBinder.class);

	interface LoadingUiBinder extends UiBinder<Widget, Loading> {
	}

	public Loading() {
		try {
			initWidget(uiBinder.createAndBindUi(this));
		} catch (Exception e) {
			GWT.log("initWidget", e);
		}
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		fireEvent(new Event.ShowLoading(true));
	}
	
	@Override
	protected void onUnload() {
		super.onUnload();
		fireEvent(new Event.ShowLoading(false));
	}
	
}