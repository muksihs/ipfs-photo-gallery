package muksihs.ipfs.photogallery.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IpfsGateway {
	private static List<IpfsGatewayEntry> gateways = new ArrayList<>();

	private static List<IpfsGatewayEntry> shuffled = new ArrayList<>();

	private static IpfsGatewayEntry fallbackPutGateway;

	public static IpfsGatewayEntry getFallbackPutGateway() {
		return fallbackPutGateway;
	}

	public static List<IpfsGatewayEntry> getGateways() {
		return gateways;
	}

	public static boolean isReady() {
		Iterator<IpfsGatewayEntry> ig = gateways.iterator();
		while (ig.hasNext()) {
			IpfsGatewayEntry g = ig.next();
			if (!g.isAlive()) {
				continue;
			}
			if (g.isWriteable()) {
				return true;
			}
		}
		return false;
	}

	public static void setFallbackPutGateway(IpfsGatewayEntry fallbackPutGateway) {
		IpfsGateway.fallbackPutGateway = fallbackPutGateway;
	}

	public static void setGateways(List<IpfsGatewayEntry> gateways) {
		IpfsGateway.gateways = gateways;
	}

	/**
	 * https://raw.githubusercontent.com/ipfs/public-gateway-checker/master/gateways.json
	 */
	public IpfsGateway() {
	}

	public IpfsGatewayEntry getAny() {
		synchronized (shuffled) {
			if (!gateways.stream().anyMatch((g) -> g.isAlive())) {
				return null;
			}
			if (shuffled.isEmpty()) {
				shuffled.addAll(gateways);
				Collections.shuffle(shuffled);
			}
			if (shuffled.isEmpty()) {
				return null;
			}
			IpfsGatewayEntry g = shuffled.remove(0);
			if (g.getBaseUrl().contains("//127.")) {
				return getAny();
			}
			if (g.getBaseUrl().contains("//localhost/")) {
				return getAny();
			}
			if (g.getBaseUrl().contains("//localhost:")) {
				return getAny();
			}
			if (g.isAlive() && !g.isWriteable()) {
				return g;
			}
			return getAny();
		}
	}

	public IpfsGatewayEntry getAnyReadonly() {
		synchronized (shuffled) {
			if (!gateways.stream().anyMatch((g) -> g.isAlive() && !g.isWriteable())) {
				return getAny();
			}
			if (shuffled.isEmpty()) {
				shuffled.addAll(gateways);
				Collections.shuffle(shuffled);
			}
			if (shuffled.isEmpty()) {
				return null;
			}
			IpfsGatewayEntry g = shuffled.remove(0);
			if (g.getBaseUrl().contains("//127.")) {
				return getAnyReadonly();
			}
			if (g.getBaseUrl().contains("//localhost/")) {
				return getAnyReadonly();
			}
			if (g.getBaseUrl().contains("//localhost:")) {
				return getAnyReadonly();
			}
			if (g.isAlive() && !g.isWriteable()) {
				return g;
			}
			return getAnyReadonly();
		}
	}

	public IpfsGatewayEntry getWritable() {
		synchronized (shuffled) {
			if (!gateways.stream().anyMatch((g) -> g.isAlive() && g.isWriteable())) {
				return getFallbackPutGateway();
			}
			if (shuffled.isEmpty()) {
				shuffled.addAll(gateways);
				Collections.shuffle(shuffled);
			}
			if (shuffled.isEmpty()) {
				return getFallbackPutGateway();
			}
			IpfsGatewayEntry g = shuffled.remove(0);
			if (g.isAlive() && g.isWriteable()) {
				return g;
			}
			return getWritable();
		}
	}
}