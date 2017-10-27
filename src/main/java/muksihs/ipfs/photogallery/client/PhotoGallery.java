package muksihs.ipfs.photogallery.client;

import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import muksihs.ipfs.photogallery.ui.MainView;
import steemjs.SteemApi;
import steemjs.TrendingTagsResult;

public class PhotoGallery implements EntryPoint {
	interface MyEventBinder extends EventBinder<PhotoGallery> {}
	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	@Override
	public void onModuleLoad() {
		Defaults.setRequestTimeout(0);
		Defaults.setAddXHttpMethodOverrideHeader(false);
		Defaults.ignoreJsonNulls();
		DeferredEventBus eventBus = new DeferredEventBus();
		eventBinder.bindEventHandlers(this, eventBus);
		MainView mainView = new MainView(eventBus);
		new PhotoGalleryApp(eventBus);
		RootPanel.get().add(mainView);
	}
	
	@EventHandler
	protected void onAppLoaded(Event.AppLoaded event) {
		GWT.log("App loaded.");
	}

	public void getTrendingTags() {
		GWT.log("getTrendingTags");
		SteemApi.getTrendingTags("", 2, (error, result) -> {
			GWT.log("getTrendingTags#callback");
			if (result != null) {
				for (TrendingTagsResult r : result) {
					StringBuilder builder = new StringBuilder();
					builder.append("TrendingTagsResult [");
					if (r.getName() != null)
						builder.append("getName()=").append(r.getName()).append(", ");
					if (r.getTotal_payouts() != null)
						builder.append("getTotal_payouts()=").append(r.getTotal_payouts()).append(", ");
					builder.append("getNet_votes()=").append(r.getNet_votes()).append(", getTop_posts()=")
							.append(r.getTop_posts()).append(", getComments()=").append(r.getComments()).append(", ");
					if (r.getTrending() != null)
						builder.append("getTrending()=").append(r.getTrending());
					builder.append("]");

					GWT.log(r.getName() + ": " + builder.toString());
				}
			} else {
				GWT.log("error: " + String.valueOf(error));
				Scheduler.get().scheduleDeferred(this::getTrendingTags);
			}
		});

	}
}
