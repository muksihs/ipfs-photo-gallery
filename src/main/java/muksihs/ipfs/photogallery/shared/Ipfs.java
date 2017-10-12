package muksihs.ipfs.photogallery.shared;

public class Ipfs {
	public static final String EMPTY_DIR = "QmUNLLsPACCz1vLxQVkXqqLX5R1X345qqfHbsf67hvA3Nn";
	public static final String HEADER_IPFS_HASH = "Ipfs-Hash";
	public static class IpfsGatewayEntry {
		private boolean writeable;
		private boolean alive;
		private String baseUrl;
		public boolean isWriteable() {
			return writeable;
		}
		public void setWriteable(boolean writeable) {
			this.writeable = writeable;
		}
		public boolean isAlive() {
			return alive;
		}
		public void setAlive(boolean alive) {
			this.alive = alive;
		}
		public String getBaseUrl() {
			return baseUrl;
		}
		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
	}
	
	public static class IpfsGateway {
		public IpfsGatewayEntry getWritable() {
			return null;
		}
		public IpfsGatewayEntry getAny() {
			return null;
		}
		
		/**
		 * https://raw.githubusercontent.com/ipfs/public-gateway-checker/master/gateways.json
		 */
		public IpfsGateway() {
		}
	}
}
