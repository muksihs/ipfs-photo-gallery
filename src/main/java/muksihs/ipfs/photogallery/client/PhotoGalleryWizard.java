package muksihs.ipfs.photogallery.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.ui.GlobalEventBus;
import muksihs.ipfs.photogallery.ui.Presenter;
import muksihs.ipfs.photogallery.ui.Presenter.ShowView;
import muksihs.ipfs.photogallery.ui.Presenter.View;

public class PhotoGalleryWizard implements ScheduledCommand, GlobalEventBus {
	
	interface MyEventBinder extends EventBinder<PhotoGalleryWizard> {}
	
	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	public PhotoGalleryWizard() {
		eventBinder.bindEventHandlers(this, eventBus);
		IpfsGatewayCache.get();
		new Presenter();
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
		}, 250);
	}
	
	@EventHandler
	protected void getAppVersion(Event.GetAppVersion event) {
		fireEvent(new Event.DisplayAppVersion("20171027"));
	}
}
