package muksihs.ipfs.photogallery.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

import muksihs.ipfs.photogallery.ui.MainView;

public class PhotoGallery implements EntryPoint {
	@Override
	public void onModuleLoad() {
		RootPanel.get().add(new MainView());
	}
}
