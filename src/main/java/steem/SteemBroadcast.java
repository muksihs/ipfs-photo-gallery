package steem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

@JsType(namespace = "steem", name = "broadcast", isNative = true)
public class SteemBroadcast {
	
	public static class Beneficiaries {
		public final List<Beneficiary> beneficiaries = new ArrayList<>();

		public String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append("{ beneficiaries: [\n");
			Iterator<Beneficiary> ib = beneficiaries.iterator();
			while (ib.hasNext()) {
				Beneficiary b = ib.next();
				sb.append("   ");
				sb.append(b.toJson());
				if (ib.hasNext()) {
					sb.append(",\n");
				}
			}
			sb.append("]}\n");
			return sb.toString();
		}
	}
	
	public static class Beneficiary {
		private final String account;
		private final int weight;

		/**
		 * 
		 * @param username Who is to receive the benefits.
		 * @param percent The amount of benefits in whole percents (no fractionals).
		 */
		public Beneficiary(String username, int percent) {
			this.account=username;
			if (percent<0) {
				percent=0;
			}
			if (percent>100) {
				percent=100;
			}
			this.weight=100*percent;
		}

		public String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append("{ \"account\": \"");
			sb.append(escapeJson(account));
			sb.append("\", \"weight\": ");
			sb.append(weight);
			sb.append("}");
			return sb.toString();
		}

	}

	public static class CommentOptionsExtensions {
		public final Beneficiaries beneficiaries = new Beneficiaries();

		public String toJson() {
			/*
			 * this is a weird looking JSON array .... [[ type_number, {} ]] and has a magic
			 * type number in it!
			 */
			StringBuilder sb = new StringBuilder();
			sb.append("[[");
			sb.append(ExtensionsType.COMMENT_PAYOUT_BENEFICIARIES.getTypeId());
			sb.append(",\n");
			sb.append(beneficiaries.toJson());
			sb.append("]]");
			return sb.toString();
		}
	}
	public static enum ExtensionsType {
	    COMMENT_PAYOUT_BENEFICIARIES(0);
		private final int typeId;
		private ExtensionsType(int typeId) {
			this.typeId=typeId;
		}
		public int getTypeId() {
			return typeId;
		}
	}

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
	public static native void comment( //
			String wif, //
			String parentAuthor, //
			String parentPermLink, //
			String author, //
			String permLink, //
			String title, //
			String body, //
			CommentMetadata metadata, //
			SteemCallback<CommentResult> callback);

	/**
	 * 
	 * @param wif
	 * @param author
	 * @param permlink
	 * @param extensions
	 * @param callback
	 */
	@JsOverlay
	public static void commentOptions(String wif, String author, //
			String permlink, CommentOptionsExtensions extensions, //
			SteemCallback<CommentResult> callback) {
		JSONValue extensionsJson = JSONParser.parseStrict(extensions.toJson());
		commentOptions( //
				wif, author, permlink, //
				"1000000.000 SBD", 10000, true, true, //
				extensionsJson, callback);
	}

	/**
	 * <code>steem.broadcast.commentOptions(wif, author, permlink, maxAcceptedPayout, percentSteemDollars, allowVotes, allowCurationRewards, extensions, function(err, result) {
	console.log(err, result);</code> });
	 */
	/**
	 * @param wif
	 * @param author
	 * @param permlink
	 * @param maxAcceptedPayout
	 * @param percentSteemDollars
	 * @param allowVotes
	 * @param allowCurationRewards
	 * @param extensions
	 * @param callback
	 */
	public static native void commentOptions(//
			String wif, //
			String author, //
			String permlink, //
			String maxAcceptedPayout, //
			int percentSteemDollars, //
			boolean allowVotes, //
			boolean allowCurationRewards, //
			JSONValue extensions, //
			SteemCallback<CommentResult> callback);

	@JsOverlay
	private static String escapeJson(String string) {
		string = string.replace("\\", "\\\\");
		string = string.replace("\"", "\\\"");
		string = string.replace("/", "\\/");
		return string;
	}
}