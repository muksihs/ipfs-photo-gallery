package steemjs;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative=true)
public abstract class TrendingTagsResult {
	@JsProperty
	public abstract String getName();
	@JsProperty
	public abstract String getTotal_payouts();
	@JsProperty
	public abstract long getNet_votes();
	@JsProperty
	public abstract long getTop_posts();
	@JsProperty
	public abstract long getComments();
	@JsProperty
	public abstract String getTrending();
}