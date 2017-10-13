package muksihs.ipfs.photogallery.shared;

public class IpfsGatewayEntry {
	private long latency=-1;
	public long getLatency() {
		return latency;
	}

	public void setLatency(long latency) {
		this.latency = latency;
	}

	public IpfsGatewayEntry(String baseUrl, boolean writeable) {
		this();
		this.baseUrl = baseUrl;
		this.writeable = writeable;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IpfsGatewayEntry)) {
			return false;
		}
		IpfsGatewayEntry other = (IpfsGatewayEntry) obj;
		if (baseUrl == null) {
			if (other.baseUrl != null) {
				return false;
			}
		} else if (!baseUrl.equals(other.baseUrl)) {
			return false;
		}
		return true;
	}

	private boolean writeable;
	private boolean alive;
	private String baseUrl;
	public IpfsGatewayEntry() {
		this.alive=false;
		this.writeable=false;
	}
	
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