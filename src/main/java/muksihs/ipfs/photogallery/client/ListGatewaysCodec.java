package muksihs.ipfs.photogallery.client;

import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.shared.GWT;

import muksihs.ipfs.photogallery.shared.StringList;

public interface ListGatewaysCodec extends JsonEncoderDecoder<StringList> {
	public static ListGatewaysCodec instance() {
		return GWT.create(ListGatewaysCodec.class);
	}
}