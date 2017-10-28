package muksihs.ipfs.photogallery.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.ui.GlobalEventBus;
import muksihs.ipfs.photogallery.ui.Presenter;
import muksihs.ipfs.photogallery.ui.Presenter.ShowView;
import muksihs.ipfs.photogallery.ui.Presenter.View;

public class PhotoGalleryWizard implements ScheduledCommand, GlobalEventBus {
	private final Presenter presenter;

	public PhotoGalleryWizard() {
		IpfsGatewayCache.get();
		presenter = new Presenter();
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
		}, 500);
	}

	public Presenter getPresenter() {
		return presenter;
	}
}
