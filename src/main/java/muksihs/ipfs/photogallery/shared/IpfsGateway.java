package muksihs.ipfs.photogallery.shared;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IpfsGateway {
	private static List<IpfsGatewayEntry> gateways;

	public static List<IpfsGatewayEntry> getGateways() {
		return gateways;
	}

	public static void setGateways(List<IpfsGatewayEntry> gateways) {
		IpfsGateway.gateways = gateways;
	}

	public IpfsGatewayEntry getWritable() {
		Collections.shuffle(gateways);
		Iterator<IpfsGatewayEntry> ig = gateways.iterator();
		while (ig.hasNext()) {
			IpfsGatewayEntry g = ig.next();
			if (!g.isAlive()) {
				continue;
			}
			if (g.isWriteable()) {
				return g;
			}
		}
		return null;
	}

	public IpfsGatewayEntry getAny() {
		Collections.shuffle(gateways);
		Iterator<IpfsGatewayEntry> ig = gateways.iterator();
		while (ig.hasNext()) {
			IpfsGatewayEntry g = ig.next();
			if (!g.isAlive()) {
				continue;
			}
			if (g.getBaseUrl().contains("//127.")) {
				continue;
			}
			if (g.getBaseUrl().contains("localhost:")) {
				continue;
			}
			return g;
		}
		return null;
	}

	/**
	 * https://raw.githubusercontent.com/ipfs/public-gateway-checker/master/gateways.json
	 */
	public IpfsGateway() {
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
}