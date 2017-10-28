package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.GenericEvent;

import muksihs.ipfs.photogallery.client.DeferredEventBus;

public interface GlobalEventBus {
	EventBus eventBus = new DeferredEventBus() {
		public void fireEvent(com.google.web.bindery.event.shared.Event<?> event) {
			if (event != null) {
				GWT.log("event: " + event.getClass().getSimpleName());
			} else {
				GWT.log("null event!");
			}
			super.fireEvent(event);
		};

		public void fireEventFromSource(com.google.web.bindery.event.shared.Event<?> event, Object source) {
			if (event != null) {
				GWT.log("event with source: " + event.getClass().getSimpleName());
			} else {
				GWT.log("null event!");
			}
			super.fireEventFromSource(event, source);
		};
	};

	default void fireEvent(GenericEvent event) {
		eventBus.fireEvent(event);
	}
}
