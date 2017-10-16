package muksihs.ipfs.photogallery.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import elemental2.core.Function;
import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequest;
import elemental2.dom.XMLHttpRequest.OnloadendCallbackFn;
import elemental2.dom.XMLHttpRequest.OnprogressCallbackFn;
import elemental2.dom.XMLHttpRequestUpload;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialRadioButton;
import muksihs.ipfs.photogallery.client.PostingTemplates;
import muksihs.ipfs.photogallery.shared.Ipfs;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.shared.IpfsGatewayEntry;

public class MainView extends Composite {

	interface MainViewUiBinder extends UiBinder<Widget, MainView> {
	}

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	private List<String> pics = new ArrayList<>();

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
			add.getElement().setAttribute("accept", "image/*");
			add.addChangeHandler((e)->{
				GWT.log("file list changed");
				HTMLInputElement x = (HTMLInputElement) DomGlobal.document.getElementById("upload");
				if (x==null) {
					Window.alert("Can't find input element!");
				}
				FileList files = x.files;
				GWT.log("Have "+files.length+" files to upload.");
				for (int ix=0; ix<files.length; ix++) {
					File f = files.getAt(ix);
				     
				    XMLHttpRequest xhr = new XMLHttpRequest();
				    IpfsGatewayEntry gw = new IpfsGateway().getWritable();
					String url = gw.getBaseUrl().replace(":hash", Ipfs.EMPTY_DIR+"/"+f.name);
					GWT.log("Posting URL: "+url);
					OnloadendCallbackFn onloadend=new OnloadendCallbackFn() {
						@Override
						public void onInvoke(ProgressEvent p0) {
							GWT.log("onloadend#state: "+xhr.readyState+", "+xhr.status);
							GWT.log("upload#onprogress: "+p0.eventPhase+", "+p0.loaded+", "+p0.total+", "+p0.type);
							GWT.log("onloadend#new hash: "+xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH));
							GWT.log("onloadend#response headers: "+xhr.getAllResponseHeaders());
						}
					};
					xhr.onloadend=onloadend;
					
					xhr.upload.onprogress=new XMLHttpRequestUpload.OnprogressCallbackFn() {
						@Override
						public void onInvoke(ProgressEvent p0) {
							GWT.log("onloadend#state: "+xhr.readyState+", "+xhr.status);
							GWT.log("upload#onprogress: "+p0.eventPhase+", "+p0.loaded+", "+p0.total+", "+p0.type);
							GWT.log("upload#response headers: "+xhr.getAllResponseHeaders());
							GWT.log("upload#ipfs hash header: "+xhr.getResponseHeader(Ipfs.HEADER_IPFS_HASH));
							GWT.log("upload#response URL: "+xhr.responseURL);
						}
					};
					GWT.log("opening connection");
					xhr.open("POST", url, true);
					GWT.log("sending data");
					xhr.send(f.slice());
					GWT.log(f.name+" ["+f.type+"] "+new java.sql.Date((long) f.lastModified));
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
		Iterator<String> iPics = pics.iterator();
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
//		GWT.log(previewHtml.toString());
	}
}
