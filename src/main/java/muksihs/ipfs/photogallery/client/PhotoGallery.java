package muksihs.ipfs.photogallery.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import muksihs.ipfs.photogallery.shared.Ipfs;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.shared.IpfsGatewayEntry;
import muksihs.ipfs.photogallery.shared.StringList;
import muksihs.ipfs.photogallery.ui.MainView;

public class PhotoGallery implements EntryPoint {
	public static final String IPFS_GATEWAY_EXPIRES = "-expires";
	private static final String IPFS_GATEWAY_LATENCY = "-latency";
	private static final String IPFS_GATEWAY_ALIVE = "-alive";
	private static final long SECOND = 1000l;
	public static final long MINUTE = SECOND * 60l;

	@Override
	public void onModuleLoad() {
		Defaults.setRequestTimeout(0);
		Defaults.setAddXHttpMethodOverrideHeader(false);
		Defaults.ignoreJsonNulls();
		GWT.log(GWT.getHostPageBaseURL());
		GWT.log(GWT.getModuleBaseURL());
		Scheduler.get().scheduleDeferred(() -> populateGateways());
		Scheduler.get().scheduleDeferred(() -> RootPanel.get().add(new MainView()));
	}

	private void pingNextGateway(Iterator<IpfsGatewayEntry> ig) {
		if (!ig.hasNext()) {
			Collections.sort(IpfsGateway.getGateways(), (a, b) -> {
				if (a.isAlive() == b.isAlive()) {
					if (a.getLatency() == b.getLatency()) {
						return a.getBaseUrl().compareTo(b.getBaseUrl());
					}
					return Long.compare(a.getLatency(), b.getLatency());
				}
				if (a.isAlive() == false) {
					return 1;
				}
				return -1;
			});
			/*
			 * Check to see if we have at least one alive writable gateway
			 */
			for (IpfsGatewayEntry g : IpfsGateway.getGateways()) {
				if (g.isAlive() && g.isWriteable()) {
					/*
					 * all is well, but schedule a retest in 5 minutes to keep
					 * alive status up-to-date
					 */
					new Timer() {
						@Override
						public void run() {
							Scheduler.get().scheduleDeferred(() -> pingGateways());
						}
					}.schedule((int) (1 * MINUTE));
					return;
				}
			}
			/*
			 * rerun again if all writable gateways show as "dead", might have
			 * been a bad Internet connection...
			 */
			resetGatewaysPingStatus();
			Scheduler.get().scheduleDeferred(() -> pingGateways());
			return;
		}
		long start = System.currentTimeMillis();
		IpfsGatewayEntry g = ig.next();
		String cookieNameExpires = cookieName(IPFS_GATEWAY_EXPIRES, g.getBaseUrl());
		String cookieNameLatency = cookieName(IPFS_GATEWAY_LATENCY, g.getBaseUrl());
		String cookieNameAlive = cookieName(IPFS_GATEWAY_ALIVE, g.getBaseUrl());
		String strExpires = Cookies.getCookie(cookieNameExpires);
		if (strExpires != null) {
			try {
				long expires = Long.valueOf(strExpires);
				if (expires > System.currentTimeMillis()) {
					String strLatency = Cookies.getCookie(cookieNameLatency);
					g.setLatency(Long.valueOf(strLatency));
					String strAlive = Cookies.getCookie(cookieNameAlive);
					g.setAlive(Boolean.valueOf(strAlive));
					pingNextGateway(ig);
				}
				return;
			} catch (NumberFormatException e) {
			}
		}
		String pingUrl = g.getBaseUrl().replace(":hash", Ipfs.EMPTY_DIR);
		RequestBuilder rb = new RequestBuilder(RequestBuilder.HEAD, pingUrl);
		rb.setTimeoutMillis(1000);
		g.setAlive(false);
		rb.setCallback(new RequestCallback() {
			@Override
			public void onResponseReceived(Request request, Response response) {
				if (response.getStatusCode() == 200) {
					g.setAlive(true);
					g.setLatency(System.currentTimeMillis() - start);
					g.setExpires(
							System.currentTimeMillis() + 15l * MINUTE + new Random().nextInt((int) (15l * MINUTE)));
					cacheIpfsGatewayStatus(g);
				}
				pingNextGateway(ig);
			}

			@Override
			public void onError(Request request, Throwable exception) {
				g.setAlive(false);
				g.setLatency(System.currentTimeMillis() - start);
				g.setExpires(System.currentTimeMillis() + 15l * MINUTE + new Random().nextInt((int) (15l * MINUTE)));
				Date expires = new Date(g.getExpires());
				Cookies.setCookie(cookieNameAlive, g.isAlive() + "", expires, null, "/", false);
				Cookies.setCookie(cookieNameLatency, g.getLatency() + "", expires, null, "/", false);
				Cookies.setCookie(cookieNameExpires, g.getExpires() + "", expires, null, "/", false);
				pingNextGateway(ig);
			}
		});
		try {
			rb.send();
		} catch (RequestException e) {
		}
	}

	private void resetGatewaysPingStatus() {
		for (IpfsGatewayEntry g : IpfsGateway.getGateways()) {
			Date expires = new Date(0);
			String cookieNameExpires = cookieName(IPFS_GATEWAY_EXPIRES, g.getBaseUrl());
			g.setExpires(0);
			Cookies.setCookie(cookieNameExpires, g.getExpires() + "", expires, null, "/", false);
		}
	}

	private static String cookieName(String dataTag, String baseUrl) {
		baseUrl = StringUtils.substringBefore(baseUrl, ":hash");
		baseUrl = StringUtils.substringAfter(baseUrl, "//");
		return baseUrl + dataTag;
	}

	private void pingGateways() {
		Iterator<IpfsGatewayEntry> ig = IpfsGateway.getGateways().iterator();
		pingNextGateway(ig);
	}

	private void populateGateways() {
		String tmp;
		StringList list;
		Gateways g = GWT.create(Gateways.class);
		List<IpfsGatewayEntry> gateways = new ArrayList<>();
		String host = Window.Location.getHost();
		if (host.equals("localhost:8080") || host.equals("127.0.0.1:8080")) {
			tmp = "{ \"list\":" + g.proxyGateways().getText() + "}";
		} else {
			/**
			 * can't round robin the writable gateways until we get at least one
			 * with CORS http headers properly set to be able to read the
			 * ipfs-hash response header. So generate a single entry
			 * representing the IPFS host we are being hosted from.
			 */
			tmp = StringUtils.substringBefore(GWT.getHostPageBaseURL(), "/ipfs/");
			tmp = "{ \"list\": [\"" + tmp + "/ipfs/:hash\"]}";
			// tmp = "{ \"list\":" + g.writableGateways().getText() + "}";
		}
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

	public static void cacheIpfsGatewayStatus(IpfsGatewayEntry g) {
		Date expires = new Date(g.getExpires());
		String cookieNameExpires = cookieName(IPFS_GATEWAY_EXPIRES, g.getBaseUrl());
		String cookieNameLatency = cookieName(IPFS_GATEWAY_LATENCY, g.getBaseUrl());
		String cookieNameAlive = cookieName(IPFS_GATEWAY_ALIVE, g.getBaseUrl());
		Cookies.setCookie(cookieNameAlive, g.isAlive() + "", expires, null, "/", false);
		Cookies.setCookie(cookieNameLatency, g.getLatency() + "", expires, null, "/", false);
		Cookies.setCookie(cookieNameExpires, g.getExpires() + "", expires, null, "/", false);
	}
}
