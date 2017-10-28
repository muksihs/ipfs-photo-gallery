package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import elemental2.dom.FileList;
import elemental2.dom.HTMLInputElement;
import muksihs.ipfs.photogallery.client.Event;

public class SelectImages extends EventBusComposite {
	
	@UiField
	protected FileUpload fileUpload;
	
	interface MyEventBinder extends EventBinder<SelectImages> {
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}
	
	@UiField
	protected HeaderBlock hblock;
	
	@EventHandler
	public void setAppVersion(Event.DisplayAppVersion event) {
		GWT.log("APP VERSION: "+event.getVersion());
		hblock.setAppVersion(event);
	}

	private static SelectImagesUiBinder uiBinder = GWT.create(SelectImagesUiBinder.class);

	interface SelectImagesUiBinder extends UiBinder<Widget, SelectImages> {
	}

	public SelectImages() {
		initWidget(uiBinder.createAndBindUi(this));
		fileUpload.getElement().setId("upload");
		fileUpload.getElement().setAttribute("multiple", "multiple");
		fileUpload.getElement().setAttribute("accept", "image/*");
		fileUpload.addChangeHandler(this::addFilesToUploadQueue);
	}
	
	private void addFilesToUploadQueue(ChangeEvent event) {
		HTMLInputElement x = (HTMLInputElement) DomGlobal.document.getElementById("upload");
		if (x == null) {
			Window.alert("Can't find input element!");
		}
		FileList files = x.files;
		GWT.log("Have " + files.length + " files to upload.");
		eventBus.fireEvent(new Event.AddImages(files));
		fileUpload.setEnabled(false);
	}

}
