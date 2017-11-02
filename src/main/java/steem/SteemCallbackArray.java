package steem;

import java.util.Map;

import jsinterop.annotations.JsFunction;

@JsFunction
public interface SteemCallbackArray<T> {
	void onResult(Map<String, String> error, T[] result);
}