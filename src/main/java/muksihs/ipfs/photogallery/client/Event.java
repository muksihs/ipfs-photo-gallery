package muksihs.ipfs.photogallery.client;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import elemental2.dom.FileList;
import muksihs.ipfs.photogallery.shared.ImageData;

public interface Event {
	public class AddImages extends GenericEvent {
		private final FileList files;

		public AddImages(FileList files) {
			this.files = files;
		}

		public FileList getFiles() {
			return files;
		}
	}

	public class AddImagesDone extends GenericEvent {

	}

	public class AddToPreviewPanel extends GenericEvent {
		private final ImageData imageData;

		public AddToPreviewPanel(ImageData imageData) {
			this.imageData = imageData;
		}

		public ImageData getImageData() {
			return imageData;
		}
	}

	public static class AlertMessage extends GenericEvent {
		private final String message;
		private final String title;

		public AlertMessage(String title, String string) {
			this.title = title;
			this.message = string;
		}

		public String getMessage() {
			return message;
		}

		public String getTitle() {
			return title;
		}
	}

	public static class AppLoaded extends GenericEvent {
	}

	public class Cancel extends GenericEvent {

	}

	public class DisplayAppVersion extends GenericEvent {
		private final String version;

		public DisplayAppVersion(String version) {
			this.version = version;
		}

		public String getVersion() {
			return version;
		}
	}

	public class EnableSelectImages extends GenericEvent {
		private final boolean enable;

		public EnableSelectImages(boolean enable) {
			this.enable = enable;
		}

		public boolean isEnable() {
			return enable;
		}
	}

	public class GalleryInfo extends GenericEvent {

		private final muksihs.ipfs.photogallery.shared.GalleryInfo galleryInfo;

		public GalleryInfo(muksihs.ipfs.photogallery.shared.GalleryInfo galleryInfo) {
			this.galleryInfo = galleryInfo;
		}

		public muksihs.ipfs.photogallery.shared.GalleryInfo getGalleryInfo() {
			return galleryInfo;
		}

	}

	public class GetAppVersion extends GenericEvent {

	}

	public class GetDescription extends GenericEvent {

	}

	public class GetGalleryInfo extends GenericEvent {

	}

	public class GetPostingKey extends GenericEvent {

	}

	public class GetTitle extends GenericEvent {

	}

	public class GetUserName extends GenericEvent {

	}

	public class ImageDataAdded extends GenericEvent {
		private final ImageData imageData;

		public ImageDataAdded(ImageData dataUrls) {
			this.imageData = dataUrls;
		}

		public ImageData getData() {
			return imageData;
		}
	}

	public static class IpfsGatewayReady extends GenericEvent {
	}

	public static class PostGallery extends GenericEvent {

		private final Integer tipAmount;
		private final String postingKey;
		private final String userName;

		public PostGallery(String userName, String postingKey, Integer tipAmount) {
			this.userName=userName;
			this.postingKey=postingKey;
			this.tipAmount=tipAmount;
		}
		
		

		public String getPostingKey() {
			return postingKey;
		}



		public Integer getTipAmount() {
			return tipAmount;
		}

		public String getUserName() {
			return userName;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PostGallery [");
			if (tipAmount != null) {
				builder.append("tipAmount=");
				builder.append(tipAmount);
				builder.append(", ");
			}
			if (postingKey != null) {
				builder.append("postingKey=");
				builder.append(postingKey);
				builder.append(", ");
			}
			if (userName != null) {
				builder.append("userName=");
				builder.append(userName);
			}
			builder.append("]");
			return builder.toString();
		}

	}

	public class PostSuccess extends GenericEvent {

		private final String author;
		private final String permLink;

		public PostSuccess(String author, String permLink) {
			this.author=author;
			this.permLink=permLink;
		}

		public String getAuthor() {
			return author;
		}

		public String getPermLink() {
			return permLink;
		}

	}

	public class RemoveImage extends GenericEvent {
		private final int index;

		public RemoveImage(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

	}

	public class SelectImagesNext extends GenericEvent {

	}

	public static class SetFilenameMsg extends GenericEvent {
		private String message;

		public SetFilenameMsg(String string) {
			this.message = string;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

	public class SetGalleryInfoNext extends GenericEvent {
	}

	public static class SetIpfsFolderLink extends GenericEvent {
		private String ipfsFolderLink;

		public SetIpfsFolderLink(String finalUrl) {
			this.ipfsFolderLink = finalUrl;
		}

		public String getIpfsFolderLink() {
			return ipfsFolderLink;
		}

		public void setIpfsFolderLink(String ipfsFolderLink) {
			this.ipfsFolderLink = ipfsFolderLink;
		}
	}

	public static class SetPreviewHtml extends GenericEvent {
		private String previewHtml;

		public SetPreviewHtml(String previewHtml) {
			this.previewHtml = previewHtml;
		}

		public String getPreviewHtml() {
			return previewHtml;
		}

		public void setPreviewHtml(String previewHtml) {
			this.previewHtml = previewHtml;
		}
	}

	public class SetPreviewTitle extends GenericEvent {

		private final String title;

		public SetPreviewTitle(String title) {
			this.title=title;
		}

		public String getTitle() {
			return title;
		}

	}

	public class SetProgress extends GenericEvent {

		private double percent;

		public SetProgress(double d) {
			this.setPercent(d);
		}

		public double getPercent() {
			return percent;
		}

		public void setPercent(double percent) {
			this.percent = percent;
		}

	}

	public class SetProgressIndeterminate extends GenericEvent {

	}

	public static class SetSteemitText extends GenericEvent {
		private String text;

		public SetSteemitText(String text2) {
			this.text = text2;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

	public class SetViewReady extends GenericEvent {

		private boolean ready;

		public SetViewReady(boolean b) {
			this.setReady(b);
		}

		public boolean isReady() {
			return ready;
		}

		public void setReady(boolean ready) {
			this.ready = ready;
		}

	}

	public class SetXhrProgress extends GenericEvent {
		private final double percent;

		public SetXhrProgress(double percent) {
			this.percent = percent;
		}

		public double getPercent() {
			return percent;
		}
	}

	public class SetXhrProgressIndeterminate extends GenericEvent {

	}

	public class ShowLoading extends GenericEvent {
		private final boolean loading;

		public ShowLoading(boolean loading) {
			this.loading = loading;
		}

		public boolean isLoading() {
			return loading;
		}

	}

	public class StoreImagesDone extends GenericEvent {

	}

	public class StoreImagesStarted extends GenericEvent {

	}

	public class UpdateImageCount extends GenericEvent {
		private final int count;

		public UpdateImageCount(int count) {
			this.count = count;
		}

		public int getCount() {
			return count;
		}

	}

	public static class UpdateUsername extends GenericEvent {

		private String username;

		public UpdateUsername(String username) {
			this.setUsername(username);
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

	}

	public static class UpdateWif extends GenericEvent {

		private String wif;

		public UpdateWif(String value) {
			this.setWif(value);
		}

		public String getWif() {
			return wif;
		}

		public void setWif(String wif) {
			this.wif = wif;
		}

	}

	public static class UploadImages extends GenericEvent {

		private FileList files;

		public UploadImages(FileList files) {
			this.setFiles(files);
		}

		public FileList getFiles() {
			return files;
		}

		public void setFiles(FileList files) {
			this.files = files;
		}

	}

	public static class WantsColumns extends GenericEvent {
		private int colummns;

		public WantsColumns(int i) {
			this.colummns = i;
		}

		public int getColummns() {
			return colummns;
		}

		public void setColummns(int colummns) {
			this.colummns = colummns;
		}

	}

	public static class WantsHtmlDisplayed extends GenericEvent {

	}

	public static class WantsNsfw extends GenericEvent {

		private boolean value;

		public WantsNsfw(boolean value) {
			this.setValue(value);
		}

		public boolean isValue() {
			return value;
		}

		public void setValue(boolean value) {
			this.value = value;
		}

	}
}
