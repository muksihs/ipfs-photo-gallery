package muksihs.ipfs.photogallery.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.RootPanel;

import muksihs.ipfs.photogallery.shared.Ipfs;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.shared.IpfsGatewayEntry;
import muksihs.ipfs.photogallery.shared.StringList;
import muksihs.ipfs.photogallery.ui.MainView;

public class PhotoGallery implements EntryPoint {
	@Override
	public void onModuleLoad() {
		Scheduler.get().scheduleDeferred(() -> populateGateways());
		Scheduler.get().scheduleDeferred(() -> RootPanel.get().add(new MainView()));
	}

	private void pingNextGateway(Iterator<IpfsGatewayEntry> ig) {
		if (!ig.hasNext()) {
			Collections.sort(IpfsGateway.getGateways(), (a, b)->{
				if (a.isAlive()==b.isAlive()) {
					if (a.getLatency()==b.getLatency()){
						return a.getBaseUrl().compareTo(b.getBaseUrl());
					}
					return Long.compare(a.getLatency(),b.getLatency());
				}
				if (a.isAlive()==false) {
					return 1;
				}
				return -1;
			});
			for (IpfsGatewayEntry g: IpfsGateway.getGateways()){
				GWT.log(g.getBaseUrl()+" ["+g.getLatency()+" ms], "+(g.isAlive()?"ALIVE":"DEAD"));
			}
			return;
		}
		long start = System.currentTimeMillis();
		IpfsGatewayEntry g = ig.next();
		GWT.log("Pinging: " + g.getBaseUrl());
		String pingUrl = g.getBaseUrl().replace(":hash", Ipfs.EMPTY_DIR);
		RequestBuilder rb = new RequestBuilder(RequestBuilder.HEAD, pingUrl);
		g.setAlive(false);
		rb.setCallback(new RequestCallback() {
			@Override
			public void onResponseReceived(Request request, Response response) {
				if (response.getStatusCode() == 200) {
					g.setAlive(true);
					g.setLatency(System.currentTimeMillis()-start);
				}
				pingNextGateway(ig);
			}

			@Override
			public void onError(Request request, Throwable exception) {
				pingNextGateway(ig);
			}
		});
		try {
			rb.send();
		} catch (RequestException e) {
		}
	}

	private void pingGateways() {
		GWT.log("pingGateways");
		Iterator<IpfsGatewayEntry> ig = IpfsGateway.getGateways().iterator();
		pingNextGateway(ig);
	}

	private void populateGateways() {
		String tmp;
		StringList list;
		Gateways g = GWT.create(Gateways.class);
		List<IpfsGatewayEntry> gateways = new ArrayList<>();
		tmp = "{ \"list\":" + g.writableGateways().getText() + "}";
		list = StringListCodec.instance().decode(JSONParser.parseStrict(tmp));
		nextGateway: for (String e : list.getList()) {
			Iterator<IpfsGatewayEntry> ig = gateways.iterator();
			while (ig.hasNext()) {
				IpfsGatewayEntry next = ig.next();
				if (next.getBaseUrl().equals(e)) {
					continue nextGateway;
				}
			}
			gateways.add(new IpfsGatewayEntry(e, true));
		}
		tmp = "{ \"list\":" + g.gateways().getText() + "}";
		list = StringListCodec.instance().decode(JSONParser.parseStrict(tmp));
		nextGateway: for (String e : list.getList()) {
			Iterator<IpfsGatewayEntry> ig = gateways.iterator();
			while (ig.hasNext()) {
				IpfsGatewayEntry next = ig.next();
				if (next.getBaseUrl().equals(e)) {
					continue nextGateway;
				}
			}
			gateways.add(new IpfsGatewayEntry(e, false));
		}
		Collections.shuffle(gateways);
		IpfsGateway.setGateways(gateways);
		Scheduler.get().scheduleDeferred(() -> pingGateways());
	}
}
