package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import elemental2.dom.FileList;
import elemental2.dom.HTMLInputElement;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialModal;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.animate.MaterialAnimation;
import gwt.material.design.client.ui.animate.Transition;
import muksihs.ipfs.photogallery.client.Event;

public class SelectImages extends EventBusComposite {

	@UiField
	protected FileUpload fileUpload;

	@UiField
	protected MaterialPanel previewPanel;

	@UiField
	protected MaterialButton next;

	@EventHandler
	protected void updatePreviewPanel(Event.AddToPreviewPanel event) {
		MaterialImage image = new MaterialImage(event.getImageDataUrl());
		image.setCaption(event.getCaption());
		image.setTitle(event.getCaption());
		image.addClickHandler((e)->showListEditOptions(image));
		previewPanel.add(image);
	}
	
	@EventHandler
	protected void updateImageCount(Event.UpdateImageCount event) {
		GWT.log("Have "+event.getCount()+" images in the gallery.");
	}

	private Void showListEditOptions(MaterialImage image) {
		int ix=0;
		for (Widget child: previewPanel.getChildren()) {
			if (child instanceof MaterialImage) {
				if (child == image) {
					break;
				}
				ix++;
			}
		}
		final int iy=ix;
		
		MaterialModal modal = new MaterialModal();
		modal.setTitle("Image Options");
		MaterialButton btnCancel = new MaterialButton("CANCEL");
		modal.addCloseHandler((e)->modal.removeFromParent());
		btnCancel.addClickHandler((e)->modal.close());
		MaterialButton btnRemove = new MaterialButton("REMOVE");
		btnRemove.addClickHandler((e)->{
			modal.getChildren().forEach(w->{
				if (w instanceof HasEnabled) {
					((HasEnabled)w).setEnabled(false);
				}
			});
			modal.setEnabled(false);
			fireEvent(new Event.RemoveImage(iy));
			MaterialAnimation anim=new MaterialAnimation();
			anim.delay(0);
			anim.duration(300);
			anim.setInfinite(false);
			anim.setTransition(Transition.FADEOUT);
			anim.animate(image, ()->{
				image.removeFromParent();
				modal.close();
			});
		});
		modal.add(btnRemove);
		modal.add(btnCancel);
		previewPanel.add(modal);
		Scheduler.get().scheduleDeferred(()->modal.open());
		return null;
	}

	@EventHandler
	protected void enable(Event.EnableSelectImages event) {
		fileUpload.setEnabled(event.isEnable());
		next.setEnabled(event.isEnable());
	}

	interface MyEventBinder extends EventBinder<SelectImages> {
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MyEventBinder getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

	@UiField
	protected HeaderBlock hblock;

	@EventHandler
	public void setAppVersion(Event.DisplayAppVersion event) {
		GWT.log("APP VERSION: " + event.getVersion());
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
			return;
		}
		FileList files = x.files;
		GWT.log("Have " + files.length + " files to upload.");
		eventBus.fireEvent(new Event.AddImages(files));
		fileUpload.setEnabled(false);
	}

}
