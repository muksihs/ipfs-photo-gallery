package steem;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace = "steem", name = "api")
public class SteemApi {
	@JsMethod
	public static native void getTrendingTags(String afterTag, int limit,
			SteemCallbackArray<TrendingTagsResult> callback);
}