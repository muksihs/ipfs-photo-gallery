package muksihs.ipfs.photogallery.ui;

import gwt.material.design.client.constants.InputType;
import gwt.material.design.client.ui.MaterialInput;

public class MaterialMultiFileUpload extends MaterialInput {
	public MaterialMultiFileUpload() {
		super(InputType.FILE);
		this.getElement().setAttribute("multiple", "multiple");
	}
}
