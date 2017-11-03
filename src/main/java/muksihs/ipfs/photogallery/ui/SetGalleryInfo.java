package muksihs.ipfs.photogallery.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import elemental2.dom.DomGlobal;
import gwt.material.design.addins.client.richeditor.MaterialRichEditor;
import gwt.material.design.addins.client.richeditor.base.constants.ToolbarButton;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialTextBox;
import muksihs.ipfs.photogallery.client.Event;
import muksihs.ipfs.photogallery.shared.GalleryInfo;

public class SetGalleryInfo extends EventBusComposite {

	interface MyEventBinder extends EventBinder<SetGalleryInfo> {
	}

	interface SetGalleryInfoUiBinder extends UiBinder<Widget, SetGalleryInfo> {
	}

	private static SetGalleryInfoUiBinder uiBinder = GWT.create(SetGalleryInfoUiBinder.class);
	@UiField
	protected MaterialTextBox title;
	@UiField
	protected MaterialTextBox tags;
	@UiField
	protected MaterialRichEditor description;
	@UiField
	protected MaterialButton cancel;

	@UiField
	protected MaterialButton next;

	public SetGalleryInfo() {
		initWidget(uiBinder.createAndBindUi(this));
		title.setAllowBlank(false);
		description.setAllowBlank(false);

		tags.setValue("photos");
		tags.addBlurHandler(this::validateTags);

		next.addClickHandler((e) -> fireEvent(new Event.SetGalleryInfoNext()));
		cancel.addClickHandler((e) -> fireEvent(new Event.Cancel()));

		ToolbarButton[] noOptions = new ToolbarButton[0];

		description.setStyleOptions(ToolbarButton.STYLE, ToolbarButton.BOLD, ToolbarButton.ITALIC,
				ToolbarButton.UNDERLINE, ToolbarButton.STRIKETHROUGH, ToolbarButton.CLEAR, ToolbarButton.SUPERSCRIPT,
				ToolbarButton.SUBSCRIPT);
		description.setFontOptions(noOptions);
		description.setColorOptions(noOptions);
		// description.setUndoOptions(noOptions);
		description.setCkMediaOptions(noOptions);
		description.setMiscOptions(ToolbarButton.LINK,
				// ToolbarButton.PICTURE,
				// ToolbarButton.TABLE,
				ToolbarButton.HR,
				// ToolbarButton.FULLSCREEN,
				ToolbarButton.CODE_VIEW);
		description.setParaOptions(ToolbarButton.UL, ToolbarButton.OL,
				// ToolbarButton.PARAGRAPH,
				ToolbarButton.LEFT, ToolbarButton.CENTER,
				// ToolbarButton.RIGHT,
				ToolbarButton.JUSTIFY);

		description.setHeightOptions(noOptions);
		description.setDisableDragAndDrop(true);
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

	@EventHandler
	protected void getGalleryInfo(Event.GetGalleryInfo event) {
		GWT.log("getGalleryInfo");
		GalleryInfo info;
		try {
			GWT.log("Title: "+title.getValue());
			GWT.log("Tags: "+tags.getValue());
			GWT.log("Description: "+description.getValue());
			List<String> tagsAsList = Arrays.asList(tags.getValue().split("\\s"));
			info = new GalleryInfo().setTitle(title.getValue()) //
					.setDescription(description.getValue()) //
					.setTags(tagsAsList);
			fireEvent(new Event.GalleryInfo(info));
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
			DomGlobal.console.log(e);
		}
	}

	protected void validateTags(BlurEvent e) {
		List<String> asList = new ArrayList<>(Arrays.asList(tags.getValue().split("\\s")));
		ListIterator<String> li = asList.listIterator();
		Set<String> already = new HashSet<>();
		while (li.hasNext()) {
			String origTag = li.next();
			String tag = origTag;
			tag = tag.toLowerCase();
			tag = tag.replaceAll("[^a-z0-9\\-]", "-");
			tag = StringUtils.strip(tag);
			while (!tag.isEmpty() && !tag.matches(".*[a-z0-9]$")) {
				tag = StringUtils.left(tag, tag.length() - 1);
			}
			while (!tag.isEmpty() && !tag.matches("[a-z].*$")) {
				tag = StringUtils.substring(tag, 1);
			}
			if (StringUtils.countMatches(tag, "-") > 1) {
				tag = StringUtils.substringBefore(tag, "-") + "-"
						+ StringUtils.substringAfter(tag, "-").replace("-", "");
			}
			if (StringUtils.isBlank(tag)) {
				fireEvent(new Event.AlertMessage("Removed Tag", origTag));
				li.remove();
				continue;
			}
			if (already.contains(tag)) {
				fireEvent(new Event.AlertMessage("Removed Duplicate Tag", origTag + " => " + tag));
				li.remove();
				continue;
			}
			if (!origTag.equals(tag)) {
				fireEvent(new Event.AlertMessage("Changed Tag", origTag + " => " + tag));
			}
			li.set(tag);
			already.add(tag);
		}
		if (asList.size() > 5) {
			fireEvent(new Event.AlertMessage("Removed Extra Tags",
					StringUtils.join(asList.subList(5, asList.size()), " ")));
			asList.subList(5, asList.size()).clear();
		}
		tags.setValue(StringUtils.join(asList, " "));
	};
}
