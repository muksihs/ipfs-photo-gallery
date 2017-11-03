package steem;

import com.google.gwt.json.client.JSONValue;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "?")
public interface SteemError {
	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "?")
	public static interface SteemErrorData {
		int getCode();
		String getMessage();
		String getName();
		JSONValue getStack();
		
	}

	int getCode();
	SteemErrorData getData();
	String getMessage();
}
