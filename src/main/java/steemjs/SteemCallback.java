package steemjs;

import java.util.Map;

import jsinterop.annotations.JsFunction;

@JsFunction
public interface SteemCallback<T> {
	void onResult(Map<String, String> error, T[] result);
}