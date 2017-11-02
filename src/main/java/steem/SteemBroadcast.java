package steem;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(name = "broadcast", namespace = "steem", isNative=true)
public class SteemBroadcast {
	/**
	 * 
	 * @param wif
	 *            Private Posting Key
	 * @param parentAuthor
	 *            Only if its a comment, the name of the post author.
	 * @param parentPermLink
	 *            If comment. For a post use main category.
	 * @param author
	 *            Author of the post/comment. You.
	 * @param permLink
	 *            Desired permalink text. Something like:
	 *            <code>new Date().toString().replaceAll("[^a-zA-Z0-9]+", '').toLowerCase()+"-"+title.replaceAll("[^a-zA-Z0-9]+", '-');</code>
	 * @param title
	 *            Title of post.
	 * @param body
	 *            Body of post/comment. Markdown or HTML.
	 * @param metadata
	 *            Metadata like tags, images, format and app.
	 * @param callback
	 *            Success or failure.
	 */
	@JsMethod
	public static native void comment(String wif, String parentAuthor, String parentPermLink, String author,
			String permLink, String title, String body, CommentMetadata metadata,
			SteemCallback<CommentResult> callback);
}