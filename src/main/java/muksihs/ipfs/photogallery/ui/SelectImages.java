package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import elemental2.dom.FileList;
import elemental2.dom.FileReader;
import elemental2.dom.HTMLInputElement;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialModal;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.animate.MaterialAnimation;
import gwt.material.design.client.ui.animate.Transition;
import muksihs.ipfs.photogallery.client.Event;

public class SelectImages extends EventBusComposite {

	interface MyEventBinder extends EventBinder<SelectImages> {
	}

	interface SelectImagesUiBinder extends UiBinder<Widget, SelectImages> {
	}

	private static SelectImagesUiBinder uiBinder = GWT.create(SelectImagesUiBinder.class);

	@UiField
	protected FileUpload fileUpload;
	
	@UiField
	protected MaterialPanel previewPanel;

	@UiField
	protected MaterialButton next;

	@UiField
	protected HeaderBlock hblock;

	public SelectImages() {
		initWidget(uiBinder.createAndBindUi(this));
		fileUpload.getElement().setId("upload");
		fileUpload.getElement().setAttribute("multiple", "multiple");
		fileUpload.getElement().setAttribute("accept", "image/*");
		fileUpload.addChangeHandler(this::addFilesToUploadQueue);
		next.setEnabled(false);
		next.addClickHandler((e)->fireEvent(new Event.SelectImagesNext()));
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
		next.setEnabled(false);
	}

	@EventHandler
	protected void enable(Event.EnableSelectImages event) {
		fileUpload.setEnabled(event.isEnable());
		next.setEnabled(event.isEnable());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MyEventBinder getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

	@EventHandler
	public void setAppVersion(Event.DisplayAppVersion event) {
		GWT.log("APP VERSION: " + event.getVersion());
		hblock.setAppVersion(event);
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
		MaterialPanel child = new MaterialPanel();
		modal.add(child);
		child.add(new MaterialImage(image.getUrl()));
		previewPanel.add(modal);
		Scheduler.get().scheduleDeferred(()->modal.open());
		return null;
	}

	@EventHandler
	protected void updateImageCount(Event.UpdateImageCount event) {
		GWT.log("Have "+event.getCount()+" images in the gallery.");
		if (event.getCount()==0) {
			next.setEnabled(false);
		}
	}

	@EventHandler
	protected void updatePreviewPanel(Event.AddToPreviewPanel event) {
		FileReader reader = new FileReader();
		reader.onloadend=(e)->{
			MaterialImage image = new MaterialImage(reader.result.asString());
			image.setCaption(event.getImageData().getName());
			image.setTitle(event.getImageData().getName());
			image.addClickHandler((e2)->showListEditOptions(image));
			previewPanel.add(image);
			return null;
		};
		reader.readAsDataURL(event.getImageData().getThumbData());
	}

}
