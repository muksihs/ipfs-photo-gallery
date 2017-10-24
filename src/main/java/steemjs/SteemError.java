package steemjs;

import com.google.gwt.json.client.JSONValue;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative=true)
public interface SteemError {
	@JsProperty
	public int getCode();
	@JsProperty
	public String getMessage();
	@JsProperty
	public SteemErrorData getData();
	
	@JsType(isNative=true)
	public static interface SteemErrorData {
		@JsProperty
		public int getCode();
		@JsProperty
		public String getName();
		@JsProperty
		public String getMessage();
		@JsProperty
		public JSONValue getStack();
	}
}
