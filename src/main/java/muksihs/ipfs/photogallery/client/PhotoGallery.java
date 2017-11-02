package muksihs.ipfs.photogallery.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import muksihs.ipfs.photogallery.ui.GlobalEventBus;
import steem.SteemApi;
import steem.TrendingTagsResult;

public class PhotoGallery implements EntryPoint, GlobalEventBus {
	interface MyEventBinder extends EventBinder<PhotoGallery> {
	}

	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	private PhotoGalleryWizard app;
	private UncaughtExceptionHandler handler=new UncaughtExceptionHandler() {
		@Override
		public void onUncaughtException(Throwable e) {
			GWT.log(e.getMessage(), e);
			DomGlobal.console.log(e.getMessage(), e);
		}
	};

	public void getTrendingTags() {
		DomGlobal.console.log("getTrendingTags");
		SteemApi.getTrendingTags("", 2, (error, result) -> {
			DomGlobal.console.log("getTrendingTags#callback");
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

					DomGlobal.console.log(r.getName() + ": " + builder.toString(), result);
				}
			} else {
				DomGlobal.console.log("error: ",error);
				Scheduler.get().scheduleDeferred(this::getTrendingTags);
			}
		});

	}

	@EventHandler
	protected void onAppLoaded(Event.AppLoaded event) {
		GWT.log("App loaded.");
		getTrendingTags();
	}

	@Override
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler(handler);
		eventBinder.bindEventHandlers(this, eventBus);
		app = new PhotoGalleryWizard();
		Scheduler.get().scheduleDeferred(app);
	}
}
