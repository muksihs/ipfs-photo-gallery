package muksihs.ipfs.photogallery.client;

import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.client.GWT;

import muksihs.ipfs.photogallery.shared.StringList;

public interface StringListCodec extends JsonEncoderDecoder<StringList> {
	public static StringListCodec instance() {
		return GWT.create(StringListCodec.class);
	}

}
