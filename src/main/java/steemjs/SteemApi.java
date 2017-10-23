package steemjs;

import jsinterop.annotations.JsType;

@JsType(namespace = "steem", name = "api")
public class SteemApi {
	public static native void getTrendingTags(String afterTag, int limit, SteemCallback<TrendingTagsResult> callback);
}