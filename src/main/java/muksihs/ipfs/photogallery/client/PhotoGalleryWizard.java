package muksihs.ipfs.photogallery.client;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.RootPanel;

public class PhotoGalleryWizard implements ScheduledCommand {
	
	private final DeferredEventBus eventbus;

	private final IpfsGatewayCache gw;
	private final RootPanel rp;

	public PhotoGalleryWizard() {
		this.eventbus = new DeferredEventBus();
		this.gw = IpfsGatewayCache.get();
		this.rp = RootPanel.get();
	}

	@Override
	public void execute() {
	}
}
