package steemjs;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative=false)
public class CommentMetadata {
	@JsProperty
	public native String[] getLinks();
	@JsProperty
	public native void setLinks(String[] links);
	@JsProperty
	public native String[] getImage();
	@JsProperty
	public native void setImage(String[] image);
	@JsProperty
	public native VideoData getVideo();
	@JsProperty
	public native void setVideo(VideoData video);
	@JsProperty
	public native String getApp();
	@JsProperty
	public native void setApp(String app);
	@JsProperty
	public native String getFormat();
	@JsProperty
	public native void setFormat(String format);
	@JsProperty
	public native String[] getTags();
	@JsProperty
	public native void setTags(String[] tags);
	@JsProperty
	public native String[] getUsers();
	@JsProperty
	public native void setUsers(String[] users);
	@JsProperty
	public native String getStatus();
	@JsProperty
	public native void setStatus(String status);
	@JsProperty	
	public native String getCanonical();
	@JsProperty
	public native void setCanonical(String canonical);
}
