package muksihs.ipfs.photogallery.client;

import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

import muksihs.ipfs.photogallery.ui.MainView;

public class PhotoGallery implements EntryPoint {

	@Override
	public void onModuleLoad() {
		Defaults.setRequestTimeout(0);
		Defaults.setAddXHttpMethodOverrideHeader(false);
		Defaults.ignoreJsonNulls();
		MainView mainView = new MainView();
		new PhotoGalleryApp(mainView);
		RootPanel.get().add(mainView);
	}
}
