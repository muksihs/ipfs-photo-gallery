package muksihs.ipfs.photogallery.client;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import elemental2.dom.FileList;

public interface Event {
	public class ShowLoading extends GenericEvent {
		private final boolean loading;

		public boolean isLoading() {
			return loading;
		}
		
		public ShowLoading(boolean loading) {
			this.loading=loading;
		}
		
	}
	public class SetProgressIndeterminate extends GenericEvent {

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
	public static class PostGallery extends GenericEvent {

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
	public static class WantsHtmlDisplayed extends GenericEvent {

	}
	public static class WantsColumns extends GenericEvent {
		private int colummns;

		public WantsColumns(int i) {
			this.colummns=i;
		}

		public int getColummns() {
			return colummns;
		}

		public void setColummns(int colummns) {
			this.colummns = colummns;
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
	public static class AlertMessage extends GenericEvent {
		private String message;

		public AlertMessage(String string) {
			this.message=string;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
	public static class SetPreviewHtml extends GenericEvent {
		public SetPreviewHtml(String previewHtml) {
			this.previewHtml=previewHtml;
		}
		private String previewHtml;

		public String getPreviewHtml() {
			return previewHtml;
		}

		public void setPreviewHtml(String previewHtml) {
			this.previewHtml = previewHtml;
		}
	}
	public static class SetIpfsFolderLink extends GenericEvent {
		private String ipfsFolderLink;

		public SetIpfsFolderLink(String finalUrl) {
			this.ipfsFolderLink=finalUrl;
		}

		public String getIpfsFolderLink() {
			return ipfsFolderLink;
		}

		public void setIpfsFolderLink(String ipfsFolderLink) {
			this.ipfsFolderLink = ipfsFolderLink;
		}
	}
	public static class SetFilenameMsg extends GenericEvent {
		private String message;

		public SetFilenameMsg(String string) {
			this.message=string;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}
	public static class SetSteemitText extends GenericEvent {
		private String text;

		public SetSteemitText(String text2) {
			this.text=text2;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
	public static class IpfsGatewayReady extends GenericEvent {}
	public static class AppLoaded extends GenericEvent{}
	public static class ViewLoaded extends GenericEvent{}
}
