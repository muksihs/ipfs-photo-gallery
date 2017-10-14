package muksihs.ipfs.photogallery.ui;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Widget;

import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.HTMLInputElement;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialRadioButton;
import jsinterop.base.Js;
import muksihs.ipfs.photogallery.client.PostingTemplates;

public class MainView extends Composite {

	interface MainViewUiBinder extends UiBinder<Widget, MainView> {
	}

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	private String[] pics = {
			"https://ipfs.io/ipfs/QmWncdt35Jci678WHJCJiwTxgTLgLknvtVyuqsgAcaEWNa/slides/slide_DSC00009.JPG",
			"https://ipfs.io/ipfs/QmWncdt35Jci678WHJCJiwTxgTLgLknvtVyuqsgAcaEWNa/slides/slide_DSC00010.JPG",
			"https://ipfs.io/ipfs/QmWncdt35Jci678WHJCJiwTxgTLgLknvtVyuqsgAcaEWNa/slides/slide_DSC00012.JPG",
			"https://ipfs.io/ipfs/QmWncdt35Jci678WHJCJiwTxgTLgLknvtVyuqsgAcaEWNa/slides/slide_DSC00013.JPG",
			"https://ipfs.io/ipfs/QmWncdt35Jci678WHJCJiwTxgTLgLknvtVyuqsgAcaEWNa/slides/slide_DSC00009.JPG",
			"https://ipfs.io/ipfs/QmWncdt35Jci678WHJCJiwTxgTLgLknvtVyuqsgAcaEWNa/slides/slide_DSC00010.JPG",
			"https://ipfs.io/ipfs/QmWncdt35Jci678WHJCJiwTxgTLgLknvtVyuqsgAcaEWNa/slides/slide_DSC00012.JPG",
			"https://ipfs.io/ipfs/QmWncdt35Jci678WHJCJiwTxgTLgLknvtVyuqsgAcaEWNa/slides/slide_DSC00013.JPG"
	};

	@UiField
	MaterialRadioButton rb1;

	@UiField
	MaterialRadioButton rb2;;

	@UiField
	MaterialRadioButton rb4;
//	@UiField
//	MaterialRadioButton rb8;

	@UiField
	FileUpload add;

	@UiField
	MaterialButton clear;
	@UiField
	MaterialButton edit;
	@UiField
	MaterialButton generate;

	@UiField
	DivElement picPreview;
	private int columns = 1;
	public MainView() {
		initWidget(uiBinder.createAndBindUi(this));
		Scheduler.get().scheduleDeferred(() -> {
			rb4.setValue(true, true);
			setColumns(4);
		});
		Scheduler.get().scheduleDeferred(()->{
			add.getElement().setId("upload");
			add.getElement().setAttribute("multiple", "multiple");
			add.addChangeHandler((e)->{
				GWT.log("file list changed");
				HTMLInputElement x = (HTMLInputElement) DomGlobal.document.getElementById("upload");
				if (x==null) {
					Window.alert("Can't find input element!");
				}
				GWT.log("HTMLInputElement x: "+x.type);
				FileList files = x.files;
				GWT.log("Have "+files.length+" files to upload.");
				for (int ix=0; ix<files.length; ix++) {
					File f = files.getAt(ix);
					GWT.log(f.name+" ["+f.type+"] "+f.lastModifiedDate);
				}
			});
		});
		Scheduler.get().scheduleDeferred(() -> {
			rb1.addClickHandler((e) -> setColumns(1));
			rb2.addClickHandler((e) -> setColumns(2));
			rb4.addClickHandler((e) -> setColumns(4));
		});
	}
	@Override
	protected void onAttach() {
		super.onAttach();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
	}

	private void setColumns(int columns) {
		this.columns = columns;
		Scheduler.get().scheduleDeferred(this::updatePreview);
		GWT.log("Columns: " + columns);
	}

	private void updatePreview() {
		int perRow;
		String template=PostingTemplates.getInstance().templates().getText();
		perRow=columns;
		switch (columns) {
		case 1:
			template = StringUtils.substringBetween(template,"<!-- 1 -->", "<!--");
			break;
		case 2:
			template = StringUtils.substringBetween(template,"<!-- 2 -->", "<!--");
			break;
		case 4:
			template = StringUtils.substringBetween(template,"<!-- 4 -->", "<!--");
			break;
		case 8:
			template = StringUtils.substringBetween(template,"<!-- 8 -->", "<!--");
			break;
		default:
			perRow=2;
			template = StringUtils.substringBetween(template,"<!-- 2 -->", "<!--");
		}
		Iterator<String> iPics = Arrays.asList(pics).iterator();
		StringBuilder previewHtml = new StringBuilder();
		String tmp = template;
		int cell = 0;
		while (iPics.hasNext()) {
			cell++;
			if (cell>perRow) {
				previewHtml.append(tmp);
				tmp = template;
				cell=1;
			}
			String pic = iPics.next();
			tmp = tmp.replace("_"+cell+"_", pic);
		}
		if (cell<=perRow) {
			while(cell<=perRow) {
				cell++;
				tmp = tmp.replace("_"+cell+"_", "https://ipfs.work/ipfs/QmXdYDa885mhijwd4Jwrnr1oX9H2M5WxxHmRsjvERhaMY4/DSC00021.JPG");
			}
			previewHtml.append(tmp);
		}
		picPreview.setInnerHTML("<div>"+previewHtml.toString()+"</div>");
		GWT.log(previewHtml.toString());
	}
}
