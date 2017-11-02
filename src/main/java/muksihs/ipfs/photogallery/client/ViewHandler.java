package muksihs.ipfs.photogallery.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.google.web.bindery.event.shared.binder.GenericEvent;

import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialLoader;
import gwt.material.design.client.ui.MaterialModal;
import gwt.material.design.client.ui.MaterialTitle;
import muksihs.ipfs.photogallery.ui.EventBusComposite;
import muksihs.ipfs.photogallery.ui.GlobalEventBus;
import muksihs.ipfs.photogallery.ui.Loading;
import muksihs.ipfs.photogallery.ui.PostGallery;
import muksihs.ipfs.photogallery.ui.SelectImages;
import muksihs.ipfs.photogallery.ui.SetGalleryInfo;
import muksihs.ipfs.photogallery.ui.UploadImages;

public class ViewHandler implements GlobalEventBus {
	public static class DisplayMessage extends GenericEvent {
		private final String message;

		public DisplayMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}

	interface MyEventBinder extends EventBinder<ViewHandler> {
	}

	public static class ShowView extends GenericEvent {
		private final View view;

		public ShowView(View view) {
			this.view = view;
		}

		public View getView() {
			return view;
		}
	}

	public static enum View {
		Loading, SelectImages, UploadImages, SetGalleryInfo, PostGallery;
	}

	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	private final RootPanel root;

	private Composite activeView;

	public ViewHandler() {
		eventBinder.bindEventHandlers(this, eventBus);
		root = RootPanel.get();
	}

	private void replaceView(EventBusComposite view) {
		if (activeView != null) {
			root.remove(activeView);
		}
		activeView = view;
		if (activeView != null) {
			root.add(activeView);
		}
	}

	@EventHandler
	protected void showAlert(Event.AlertMessage event) {
		MaterialModal alert = new MaterialModal();
		alert.setTitle("ALERT!");
		alert.add(new MaterialTitle(event.getTitle(), event.getMessage()));
		alert.addCloseHandler((e) -> alert.removeFromParent());
		MaterialButton btn = new MaterialButton("OK");
		btn.addClickHandler((e) -> alert.close());
		alert.add(btn);
		RootPanel.get().add(alert);
		alert.open();
	}

	@EventHandler
	protected void showLoading(Event.ShowLoading event) {
		MaterialLoader.loading(event.isLoading());
	}

	@EventHandler
	protected void showView(ShowView event) {
		GWT.log("view: " + String.valueOf(event.view));
		switch (event.getView()) {
		case SetGalleryInfo:
			replaceView(new SetGalleryInfo());
			break;
		case Loading:
			replaceView(new Loading());
			break;
		case PostGallery:
			replaceView(new PostGallery());
			break;
		case SelectImages:
			replaceView(new SelectImages());
			break;
		case UploadImages:
			replaceView(new UploadImages());
			break;
		}
	}
}
