package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import elemental2.dom.DomGlobal;
import elemental2.dom.FileList;
import elemental2.dom.HTMLInputElement;
import gwt.material.design.client.constants.ProgressType;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialCheckBox;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialProgress;
import gwt.material.design.client.ui.MaterialRadioButton;
import gwt.material.design.client.ui.MaterialTextBox;
import gwt.material.design.client.ui.MaterialToast;
import muksihs.ipfs.photogallery.shared.Consts;
import muksihs.ipfs.photogallery.shared.PhotoGalleryController;

public class MainView extends Composite implements muksihs.ipfs.photogallery.shared.View {

	interface MainViewUiBinder extends UiBinder<Widget, MainView> {
	}

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	@UiField
	MaterialLabel version;
	
	@UiField
	MaterialLabel filename;

	@UiField
	MaterialProgress progress;

	@UiField
	MaterialRadioButton rb1;

	@UiField
	MaterialRadioButton rb2;;

	@UiField
	MaterialRadioButton rb4;

	@UiField
	FileUpload add;

	@UiField
	MaterialButton clear;
	@UiField
	MaterialButton edit;
	@UiField
	MaterialButton showSteemitText;

	@UiField
	MaterialLink link;

	@UiField
	DivElement picPreview;
	
	public void setProgress(double percent){
		progress.setType(ProgressType.DETERMINATE);
		progress.setPercent(Math.ceil(percent));
	}
	
	public void setProgressIndeterminate(){
		progress.setPercent(0);
		progress.setType(ProgressType.INDETERMINATE);
	}

	private void uploadImages() {
		GWT.log("view: uploadImages");
		HTMLInputElement x = (HTMLInputElement) DomGlobal.document.getElementById("upload");
		if (x == null) {
			Window.alert("Can't find input element!");
		}
		FileList files = x.files;
		GWT.log("Have " + files.length + " files to upload.");
		try {
			app.uploadImages(files);
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
		link.setEnabled(false);
		add.setEnabled(false);
	}
	@UiField
	protected MaterialTextBox username;
	@UiField
	protected MaterialTextBox wif;
	@UiField
	protected MaterialButton post;
	
	@UiField
	protected MaterialCheckBox nsfw;
	public MainView() {
		initWidget(uiBinder.createAndBindUi(this));
		
		version.setText(Consts.VERSION);
		
		rb4.setValue(true, true);
		
		add.getElement().setId("upload");
		add.getElement().setAttribute("multiple", "multiple");
		add.getElement().setAttribute("accept", "image/*");
		add.addChangeHandler((e)->uploadImages());
		
		rb1.addClickHandler((e) -> app.wantsColumns(1));
		rb2.addClickHandler((e) -> app.wantsColumns(2));
		rb4.addClickHandler((e) -> app.wantsColumns(4));
		showSteemitText.addClickHandler((e) -> app.wantsHtmlDisplayed());
		clear.addClickHandler((e) -> Location.reload());
		edit.setVisible(false);
		edit.setEnabled(false);
		add.setEnabled(false);
		
		nsfw.addClickHandler((e)->app.wantsNsfw(nsfw.getValue()));
		wif.addChangeHandler((e)->app.updateWif(wif.getValue()));
		username.addChangeHandler((e)->app.updateUsername(username.getValue()));
		post.addClickHandler((e)->app.postGallery());
	}
	
	@Override
	public void setReady(boolean ready) {
		add.setEnabled(ready);
	}

	@UiField
	TextArea steemitText;

	@Override
	public void setSteemitHtml(String text){
		steemitText.setText(text);
	}

	private PhotoGalleryController app;

	@Override
	protected void onAttach() {
		super.onAttach();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
	}

	@Override
	public void setFileText(String msg) {
		filename.setText(msg);
	}

	@Override
	public void setIpfsFolderLink(String finalUrl) {
		link.setEnabled(true);
		link.setHref(finalUrl);
		link.getElement().setAttribute("target", "_blank");
		add.setEnabled(true);
	}

	@Override
	public void setPreviewHtml(String steemitHtml) {
		this.picPreview.setInnerHTML(steemitHtml);
	}

	@Override
	public void setController(PhotoGalleryController app) {
		this.app=app;
	}

	@Override
	public void alert(String message) {
		MaterialToast.fireToast(message, 10000);
	}
}
