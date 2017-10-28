package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.web.bindery.event.shared.binder.EventBinder;

public abstract class EventBusComposite extends Composite implements GlobalEventBus {

	interface MyEventBinder extends EventBinder<EventBusComposite> {
	}

	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	public EventBusComposite() {
		eventBinder.bindEventHandlers(this, eventBus);
	}

}
