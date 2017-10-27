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
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

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
import muksihs.ipfs.photogallery.client.Event;
import muksihs.ipfs.photogallery.shared.Consts;

public class MainView extends Composite {

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

	public void setProgress(double percent) {
		progress.setType(ProgressType.DETERMINATE);
		progress.setPercent(Math.ceil(percent));
	}

	public void setProgressIndeterminate() {
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
			eventBus.fireEvent(new Event.UploadImages(files));
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

	private EventBus eventBus;

	public MainView(EventBus eventBus) {
		initWidget(uiBinder.createAndBindUi(this));
		this.eventBus = eventBus;

		version.setText(Consts.VERSION);

		rb4.setValue(true, true);

		add.getElement().setId("upload");
		add.getElement().setAttribute("multiple", "multiple");
		add.getElement().setAttribute("accept", "image/*");
		add.addChangeHandler((e) -> uploadImages());

		rb1.addClickHandler((e) -> eventBus.fireEvent(new Event.WantsColumns(1)));
		rb2.addClickHandler((e) -> eventBus.fireEvent(new Event.WantsColumns(2)));
		rb4.addClickHandler((e) -> eventBus.fireEvent(new Event.WantsColumns(4)));
		showSteemitText.addClickHandler((e) -> eventBus.fireEvent(new Event.WantsHtmlDisplayed()));
		clear.addClickHandler((e) -> Location.reload());
		edit.setVisible(false);
		edit.setEnabled(false);
		add.setEnabled(false);

		nsfw.addClickHandler((e) -> eventBus.fireEvent(new Event.WantsNsfw(nsfw.getValue())));
		wif.addChangeHandler((e) -> eventBus.fireEvent(new Event.UpdateWif(wif.getValue())));
		username.addChangeHandler((e) -> eventBus.fireEvent(new Event.UpdateUsername(username.getValue())));
		post.addClickHandler((e) -> eventBus.fireEvent(new Event.PostGallery()));
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		eventBinder.bindEventHandlers(this, eventBus);
		eventBus.fireEvent(new Event.ViewLoaded());
	}
	
	interface MyEventBinder extends EventBinder<MainView> {}
	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	@EventHandler
	public void setReady(Event.IpfsGatewayReady ready) {
		add.setEnabled(true);
	}

	@UiField
	TextArea steemitText;

	@EventHandler
	public void setSteemitHtml(Event.SetSteemitText event) {
		steemitText.setText(event.getText());
	}

	@Override
	protected void onAttach() {
		super.onAttach();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
	}

	@EventHandler
	public void setFileText(Event.SetFilenameMsg event) {
		filename.setText(event.getMessage());
	}

	@EventHandler
	public void setIpfsFolderLink(Event.SetIpfsFolderLink event) {
		link.setEnabled(true);
		link.setHref(event.getIpfsFolderLink());
		link.getElement().setAttribute("target", "_blank");
		add.setEnabled(true);
	}

	@EventHandler
	public void setPreviewHtml(Event.SetPreviewHtml event) {
		this.picPreview.setInnerHTML(event.getPreviewHtml());
	}

	@EventHandler
	public void alert(Event.AlertMessage event) {
		MaterialToast.fireToast(event.getMessage(), 10000);
	}
}
