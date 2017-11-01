package muksihs.ipfs.photogallery.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.addins.client.richeditor.MaterialRichEditor;
import gwt.material.design.addins.client.richeditor.base.constants.ToolbarButton;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialTextBox;
import muksihs.ipfs.photogallery.client.Event;
import muksihs.ipfs.photogallery.shared.GalleryInfo;

public class SetGalleryInfo extends EventBusComposite {

	private static SetGalleryInfoUiBinder uiBinder = GWT.create(SetGalleryInfoUiBinder.class);

	interface SetGalleryInfoUiBinder extends UiBinder<Widget, SetGalleryInfo> {
	}
	
	@UiField
	protected MaterialTextBox title;
	@UiField
	protected MaterialRichEditor description;
	@UiField
	protected MaterialTextBox username;
	@UiField
	protected MaterialTextBox key;
	@UiField
	protected MaterialButton cancel;
	@UiField
	protected MaterialButton next;
	@UiField
	protected MaterialTextBox tags;
	
	@EventHandler
	protected void getGalleryInfoPageValues(Event.GetGalleryInfoPageValues event) {
		GalleryInfo galleryInfo = new GalleryInfo();
		galleryInfo.setTitle(title.getValue());
		galleryInfo.setDescription(description.getValue());
		galleryInfo.setUserName(username.getValue());
		galleryInfo.setPostingKey(key.getValue());
		galleryInfo.setTitle(tags.getValue());
		fireEvent(new Event.GalleryInfo(galleryInfo));
	}
	
	public SetGalleryInfo() {
		initWidget(uiBinder.createAndBindUi(this));
		title.setAllowBlank(false);
		description.setAllowBlank(false);
		
		next.setEnabled(false);
		next.addClickHandler((e)->fireEvent(new Event.SetGalleryInfoNext()));
		cancel.addClickHandler((e)->fireEvent(new Event.Cancel()));
		
		ToolbarButton[] noOptions = new ToolbarButton[0];
		
		description.setStyleOptions(ToolbarButton.STYLE,
				ToolbarButton.BOLD,
				ToolbarButton.ITALIC,
				ToolbarButton.UNDERLINE,
				ToolbarButton.STRIKETHROUGH,
				ToolbarButton.CLEAR,
				ToolbarButton.SUPERSCRIPT,
				ToolbarButton.SUBSCRIPT);
		description.setFontOptions(noOptions);
		description.setColorOptions(noOptions);
//		description.setUndoOptions(noOptions);
		description.setCkMediaOptions(noOptions);
		description.setMiscOptions(ToolbarButton.LINK,
//				ToolbarButton.PICTURE,
//				ToolbarButton.TABLE,
				ToolbarButton.HR,
//				ToolbarButton.FULLSCREEN,
				ToolbarButton.CODE_VIEW);
		description.setParaOptions(ToolbarButton.UL,
				ToolbarButton.OL,
//				ToolbarButton.PARAGRAPH,
				ToolbarButton.LEFT,
				ToolbarButton.CENTER,
//				ToolbarButton.RIGHT,
				ToolbarButton.JUSTIFY);
		
		description.setHeightOptions(noOptions);
		description.setDisableDragAndDrop(true);
	}

	interface MyEventBinder extends EventBinder<SetGalleryInfo>{};
	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

}
