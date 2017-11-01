package muksihs.ipfs.photogallery.shared;

import java.util.Date;

public class IpfsGatewayEntry {
	private Date expires;
	private long latency = -1;
	private int fails = 0;

	private boolean writeable;
	private boolean alive;
	private String baseUrl;

	public IpfsGatewayEntry() {
		this.alive = false;
		this.writeable = false;
	}

	public IpfsGatewayEntry(String baseUrl, boolean writeable) {
		this();
		setBaseUrl(baseUrl);
		setWriteable(writeable);
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

	public void fail() {
		fails++;
		if (fails > 2) {
			setAlive(false);
		}
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public Date getExpires() {
		if (expires == null) {
			return new Date(0);
		}
		return expires;
	}

	public long getLatency() {
		return latency;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
		return result;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isWriteable() {
		return writeable;
	}

	public void resetFail() {
		fails = 0;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
		if (alive) {
			fails = 0;
		}
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public void setLatency(long latency) {
		this.latency = latency;
	}

	public void setWriteable(boolean writeable) {
		this.writeable = writeable;
	}
}