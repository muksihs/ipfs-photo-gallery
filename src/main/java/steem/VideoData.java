package steem;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = false)
public class VideoData {
	@JsProperty
	public native VideoContent getContent();

	@JsProperty
	public native VideoInfo getInfo();

	@JsProperty
	public native void setContent(VideoContent content);

	@JsProperty
	public native void setInfo(VideoInfo info);
}
