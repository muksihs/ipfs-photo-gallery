package steem;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace = "steem", name = "api", isNative=true)
public class SteemApi {
	public static native void getTrendingTags(String afterTag, int limit,
			SteemCallbackArray<TrendingTagsResult> callback);
}