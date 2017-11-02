package steem;

import jsinterop.annotations.JsFunction;

@JsFunction
public interface SteemCallback<T> {
	void onResult(SteemError error, T result);
}