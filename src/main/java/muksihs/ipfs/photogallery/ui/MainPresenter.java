package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

public class MainPresenter extends Composite {

	private static MainPresenterUiBinder uiBinder = GWT.create(MainPresenterUiBinder.class);

	interface MainPresenterUiBinder extends UiBinder<Widget, MainPresenter> {
	}
	
	public MainPresenter(EventBus eventBus) {
		initWidget(uiBinder.createAndBindUi(this));
	}
}
