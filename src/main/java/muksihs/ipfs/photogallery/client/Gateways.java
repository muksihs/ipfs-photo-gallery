package muksihs.ipfs.photogallery.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface Gateways extends ClientBundle {

	@Source("muksihs/ipfs/photogallery/shared/gateways.json")
	TextResource gateways();

	@Source("muksihs/ipfs/photogallery/shared/proxy-gateways.json")
	TextResource proxyGateways();

	@Source("muksihs/ipfs/photogallery/shared/writable-gateways.json")
	TextResource writableGateways();

}
