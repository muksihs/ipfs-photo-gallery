package steem;

import com.google.gwt.json.client.JSONValue;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface SteemError {
	@JsType(isNative = true)
	public static interface SteemErrorData {
		@JsProperty
		public int getCode();

		@JsProperty
		public String getMessage();

		@JsProperty
		public String getName();

		@JsProperty
		public JSONValue getStack();
	}

	@JsProperty
	public int getCode();

	@JsProperty
	public SteemErrorData getData();

	@JsProperty
	public String getMessage();
}
