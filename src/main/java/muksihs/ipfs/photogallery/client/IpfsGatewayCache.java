package muksihs.ipfs.photogallery.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

import muksihs.ipfs.photogallery.shared.Consts;
import muksihs.ipfs.photogallery.shared.IpfsGateway;
import muksihs.ipfs.photogallery.shared.IpfsGatewayEntry;
import muksihs.ipfs.photogallery.shared.StringList;
import muksihs.ipfs.photogallery.ui.GlobalEventBus;

public class IpfsGatewayCache implements GlobalEventBus {
	protected interface IgCodec extends JsonEncoderDecoder<IpfsGatewayEntry> {

	}
	private static final int SECOND = 1000;

	public static final int MINUTE = SECOND * 60;
	private static final String IPFS_GATEWAY_EXPIRES = "-expires";
	private static final String IPFS_GATEWAY_LATENCY = "-latency";
	private static final String IPFS_GATEWAY_ALIVE = "-alive";

	private static IpfsGatewayCache instance;

	public static IpfsGatewayCache get() {
		if (instance == null) {
			instance = new IpfsGatewayCache();
			populateGateways();
			loadFromCache();
		}
		return instance;
	}

	private static void loadFromCache() {
		StorageMap cache = instance.cache;
		IpfsGateway.getGateways().forEach((g) -> {
			String jsonTxt = cache.get(IpfsGatewayEntry.class.getSimpleName() + ":" + g.getBaseUrl());
			IpfsGatewayEntry cached = instance.codec.decode(JSONParser.parseStrict(jsonTxt));
			if (cached != null) {
				g.setAlive(cached.isAlive());
				g.setExpires(cached.getExpires());
				g.setLatency(cached.getLatency());
			}
		});
	}

	private static void populateGateways() {
		String tmp;
		StringList list;
		Gateways g = GWT.create(Gateways.class);
		List<IpfsGatewayEntry> gateways = new ArrayList<>();
		String host = Window.Location.getHost();
		/*
		 * First populate writable gateways.
		 */
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
			tmp = "{ \"list\": [\"" + tmp + "/ipfs/\"]}";
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
		/*
		 * stash one of the writable gateways as a fallback
		 */
		Collections.shuffle(gateways);
		IpfsGateway.setFallbackPutGateway(gateways.get(0));
		/*
		 * Add all other gateways now.
		 */
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
	}

	private final StorageMap cache;

	private IgCodec codec;

	protected IpfsGatewayCache() {
		cache = new StorageMap(Storage.getLocalStorageIfSupported());
		codec = GWT.create(IgCodec.class);
		Scheduler.get().scheduleDeferred(this::globalExpiresCheck);
		Scheduler.get().scheduleDeferred(this::legacyCookieCleanup);
		Scheduler.get().scheduleDeferred(this::startWatchdogs);
	}

	public void cacheIpfsGatewayStatus(IpfsGatewayEntry g) {
		cacheIpfsGatewayStatus(g, true);
	}

	public void cacheIpfsGatewayStatus(IpfsGatewayEntry g, boolean setNewExpires) {
		// set a random cache time
		if (setNewExpires) {
			long expires = System.currentTimeMillis() + new Random().nextInt(15 * MINUTE);
			g.setExpires(new Date(expires));
		}
		cache.put(IpfsGatewayEntry.class.getSimpleName() + ":" + g.getBaseUrl(), codec.encode(g).toString());
	}

	public RequestCallback checkCallback(Iterator<IpfsGatewayEntry> ig, IpfsGatewayEntry g, long start) {
		return new RequestCallback() {
			@Override
			public void onError(Request request, Throwable exception) {
				g.setAlive(false);
				g.setLatency(System.currentTimeMillis() - start);
				cacheIpfsGatewayStatus(g);
				pingNextGateway(ig);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				if (response.getStatusCode() == 200) {
					g.setAlive(true);
					g.setLatency(System.currentTimeMillis() - start);
					cacheIpfsGatewayStatus(g);
				} else {
					onError(request, new RuntimeException(response.getStatusText()));
				}
				pingNextGateway(ig);
			}
		};
	}

	public void forceExpireGateways() {
		for (IpfsGatewayEntry g : IpfsGateway.getGateways()) {
			g.setExpires(new Date(0));
			cacheIpfsGatewayStatus(g, false);
		}
	}

	public Comparator<? super IpfsGatewayEntry> gatewaySorter() {
		return (a, b) -> {
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
		};
	}

	public void globalExpiresCheck() {
		/*
		 * check global expires timer to see if we need to reset all IpfsGateway
		 * entries.
		 */
		Date expires;
		try {
			expires = new Date(Long.valueOf(cache.get(IpfsGatewayCache.class.getName())));
		} catch (NumberFormatException e) {
			expires = new Date(System.currentTimeMillis() - 1);
		}
		if (expires.before(new Date())) {
			for (String key : cache.keySet()) {
				if (!key.startsWith(IpfsGatewayEntry.class.getSimpleName())) {
					continue;
				}
				cache.remove(key);
			}
		}
		cache.put(IpfsGatewayCache.class.getName(),String.valueOf(System.currentTimeMillis()+24*60*MINUTE));
	}

	private void legacyCookieCleanup() {
		for (String c : Cookies.getCookieNames()) {
			if (c.endsWith(IPFS_GATEWAY_EXPIRES)) {
				Cookies.setCookie(c, "", new Date());
			}
			if (c.endsWith(IPFS_GATEWAY_LATENCY)) {
				Cookies.setCookie(c, "", new Date());
			}
			if (c.endsWith(IPFS_GATEWAY_ALIVE)) {
				Cookies.setCookie(c, "", new Date());
			}
		}
	}

	public boolean pingGateways() {
		Iterator<IpfsGatewayEntry> ig = IpfsGateway.getGateways().iterator();
		Scheduler.get().scheduleDeferred(() -> pingNextGateway(ig));
		return true;
	}

	private void pingNextGateway(Iterator<IpfsGatewayEntry> ig) {
		if (!ig.hasNext()) {
			Collections.sort(IpfsGateway.getGateways(), gatewaySorter());
			return;
		}
		long start = System.currentTimeMillis();
		IpfsGatewayEntry g = ig.next();
		if (g.getExpires().after(new Date())) {
			Scheduler.get().scheduleDeferred(() -> pingNextGateway(ig));
			return;
		}
		String testImg;
		if (new Random().nextBoolean()) {
			testImg=Consts.NSFW;
		} else {
			testImg=Consts.PLACEHOLDER;
		}
		String pingUrl = g.getBaseUrl()+ testImg;
		RequestBuilder rb = new RequestBuilder(RequestBuilder.HEAD, pingUrl);
		rb.setTimeoutMillis(1000);
		g.setAlive(false);
		RequestCallback callback = checkCallback(ig, g, start);
		rb.setCallback(callback);
		try {
			rb.send();
		} catch (RequestException e) {
			callback.onError(null, null);
		}
	}

	private void startWatchdogs() {
		Scheduler.get().scheduleFixedDelay(this::watchDog, 1000);
		Scheduler.get().scheduleFixedDelay(this::pingGateways, 30000);
	}

	/**
	 * Check to see if we have at least one alive writable gateway, if no
	 * writable gateways exists, force expire all gateways
	 * 
	 * @return
	 */
	private boolean watchDog() {
		for (IpfsGatewayEntry g : IpfsGateway.getGateways()) {
			if (g.isAlive() && g.isWriteable()) {
				return true;
			}
		}
		forceExpireGateways();
		pingGateways();
		return true;
	}
}
