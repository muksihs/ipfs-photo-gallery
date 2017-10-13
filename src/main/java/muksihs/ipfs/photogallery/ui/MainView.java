package muksihs.ipfs.photogallery.ui;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import gwt.material.design.client.constants.InputType;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialInput;
import gwt.material.design.client.ui.MaterialRadioButton;
import muksihs.ipfs.photogallery.client.PostingTemplates;

public class MainView extends Composite {

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	interface MainViewUiBinder extends UiBinder<Widget, MainView> {
	}

	public MainView() {
		initWidget(uiBinder.createAndBindUi(this));
		Scheduler.get().scheduleDeferred(() -> {
			rb1.addClickHandler((e) -> setColumns(1));
			rb2.addClickHandler((e) -> setColumns(2));
			rb4.addClickHandler((e) -> setColumns(4));
			add.addClickHandler((e) -> upload());
//			rb8.addClickHandler((e) -> setColumns(8));
		});
		Scheduler.get().scheduleDeferred(() -> {
			rb4.setValue(true, true);
			setColumns(4);
		});
	}

	private Object upload() {
		FileUpload fileUpload = new FileUpload();
		fileUpload.getElement().setAttribute("multiple", "multiple");
		RootPanel.get().add(fileUpload);
		fileUpload.addChangeHandler((e)->{
			GWT.log("fileupload-change: "+fileUpload.getFilename());
			JsArray<JavaScriptObject> files = fileUpload.getElement().getPropertyJSO("files").cast();
			if (files!=null) {
				for (int ix=0; ix<files.length(); ix++) {
					GWT.log(ix+": "+files.get(ix).toString());
					GWT.log(ix+": "+files.get(ix).toSource());
				}
			}
		});
		return null;
	}

	private void setColumns(int columns) {
		this.columns = columns;
		Scheduler.get().scheduleDeferred(this::updatePreview);
		GWT.log("Columns: " + columns);
	};

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

	@UiField
	MaterialRadioButton rb1;
	@UiField
	MaterialRadioButton rb2;
	@UiField
	MaterialRadioButton rb4;
//	@UiField
//	MaterialRadioButton rb8;

	@UiField
	MaterialMultiFileUpload add;
	@UiField
	MaterialButton clear;
	@UiField
	MaterialButton edit;
	@UiField
	MaterialButton generate;

	@UiField
	DivElement picPreview;

	private int columns = 1;

	@Override
	protected void onAttach() {
		super.onAttach();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
	}

}
