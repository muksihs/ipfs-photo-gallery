package steem;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = false)
public class VideoContent {
	@JsProperty
	public native String getDescription();

	@JsProperty
	public native String[] getTags();

	@JsProperty
	public native String getVideohash();

	@JsProperty
	public native void setDescription(String description);

	@JsProperty
	public native void setTags(String[] tags);

	@JsProperty
	public native void setVideohash(String videohash);
}
