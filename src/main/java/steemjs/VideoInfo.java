package steemjs;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative=false)
public class VideoInfo {
	@JsProperty
	public native String getSnaphash();
	@JsProperty
	public native void setSnaphash(String snaphash);
	@JsProperty
	public native String getPermlink();
	@JsProperty
	public native void setPermlink(String permlink);
	@JsProperty
	public native String getTitle();
	@JsProperty
	public native void setTitle(String title);
	@JsProperty
	public native String getAuthor();
	@JsProperty
	public native void setAuthor(String author);
}
