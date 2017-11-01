package muksihs.ipfs.photogallery.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface PostingTemplates extends ClientBundle {
	public static PostingTemplates getInstance() {
		return GWT.create(PostingTemplates.class);
	}

	@Source("muksihs/ipfs/photogallery/shared/posting-templates.html")
	TextResource templates();
}
