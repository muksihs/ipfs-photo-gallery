package muksihs.ipfs.photogallery.shared;

public class Consts {
	public static final int KB = 1024;
	public static final double maxSize = 256;
	public static final double jpgQuality = 0.7;
	public static final String PLACEHOLDER_HASH = "QmQ4keX7r9YnoARDgq4YJBqRwABcfXsnnE8EkD5EnjtLVH";
	public static final String PLACEHOLDER = "QmQ4keX7r9YnoARDgq4YJBqRwABcfXsnnE8EkD5EnjtLVH/placeholder.png";
	public static final String NSFW = "QmduViEznLataN9tdWm6CntnbBCVG8vgQrpXrrC7n79qv7/1-nsfw.png";
	public static final String VERSION = "20171023";
	public static final String TEST_HTML = "<p style=\"text-align: left; \">\n" + 
			"    <span style=\"font-weight: bold;\">\n" + 
			"        &lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" + 
			"    </span>\n" + 
			"</p>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span style=\"font-weight: bold; font-style: italic;\">\n" + 
			"        &lt;!DOCTYPE\n" + 
			"    </span>\n" + 
			"</p>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span style=\"font-style: italic;\">\n" + 
			"        &nbsp;module\n" + 
			"    </span>\n" + 
			"</p>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span style=\"font-style: italic; text-decoration: underline;\">\n" + 
			"        &nbsp;PUBLIC\n" + 
			"    </span>\n" + 
			"</p>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span style=\"text-decoration: underline line-through;\">\n" + 
			"        &nbsp;\"-//Google Inc.//DTD Google Web Toolkit 2.8.1//EN\"\n" + 
			"    </span>\n" + 
			"</p>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span style=\"font-weight: bold; font-style: italic; text-decoration: underline line-through;\">\n" + 
			"        &nbsp;\"https://raw.githubusercontent.com/gwtproject/gwt/master/distro-source/core/src/gwt-module.dtd\"&gt;\n" + 
			"    </span>\n" + 
			"</p>\n" + 
			"<blockquote style=\"text-align: left; \">\n" + 
			"    &lt;module rename-to='PhotoGallery'&gt;\n" + 
			"</blockquote>\n" + 
			"<pre style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space: pre;\">\n" + 
			"</span>\n" + 
			"    &lt;entry-point class='muksihs.ipfs.photogallery.client.PhotoGallery' /&gt;\n" + 
			"</pre>\n" + 
			"<h1 style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space: pre;\">\n" + 
			"</span>\n" + 
			"    &lt;source path='client' /&gt;\n" + 
			"</h1>\n" + 
			"<h2 style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;source path='shared' /&gt;\n" + 
			"</h2>\n" + 
			"<h3 style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;source path='ui' /&gt;\n" + 
			"</h3>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"</p>\n" + 
			"<h4 style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;!-- eventbinder --&gt;\n" + 
			"</h4>\n" + 
			"<h5 style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;inherits name='com.google.web.bindery.event.EventBinder'/&gt;\n" + 
			"</h5>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"</p>\n" + 
			"<h6 style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;!-- steemjs --&gt;\n" + 
			"</h6>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;inherits name=\"\n" + 
			"    <span style=\"vertical-align: super;\">\n" + 
			"        steemjs.SteemJs\n" + 
			"    </span>\n" + 
			"    \" /&gt;\n" + 
			"</p>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;!-- HTML5 --&gt;\n" + 
			"</p>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;inherits name=\"\n" + 
			"    <span style=\"vertical-align: sub;\">\n" + 
			"        elemental2.dom.Dom\n" + 
			"    </span>\n" + 
			"    \" /&gt;\n" + 
			"</p>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <br>\n" + 
			"</p>\n" + 
			"<ul>\n" + 
			"    <li style=\"text-align: left;\">\n" + 
			"        <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"    </span>\n" + 
			"        &lt;!-- UI --&gt;\n" + 
			"    </li>\n" + 
			"    <li style=\"text-align: left;\">\n" + 
			"        <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"    </span>\n" + 
			"        &lt;inherits name=\"gwt.material.design.GwtMaterialWithJQuery\" /&gt;\n" + 
			"    </li>\n" + 
			"    <li style=\"text-align: left;\">\n" + 
			"        <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"    </span>\n" + 
			"        &lt;inherits name=\"gwt.material.design.themes.ThemeBlue\" /&gt;\n" + 
			"    </li>\n" + 
			"    <li style=\"text-align: left;\">\n" + 
			"        <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"    </span>\n" + 
			"        &lt;inherits name=\"gwt.material.design.addins.GwtMaterialAddins\" /&gt;\n" + 
			"    </li>\n" + 
			"</ul>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"</p>\n" + 
			"<ol>\n" + 
			"    <li style=\"text-align: left;\">\n" + 
			"        <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"    </span>\n" + 
			"        &lt;set-configuration-property name=\"CssResource.conversionMode\"\n" + 
			"    </li>\n" + 
			"    <li style=\"text-align: left;\">\n" + 
			"        <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"    </span>\n" + 
			"        value=\"strict\" /&gt;\n" + 
			"    </li>\n" + 
			"    <li style=\"text-align: left;\">\n" + 
			"        <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"    </span>\n" + 
			"        &lt;!-- &lt;inherits name=\"gwt.material.design.themes.ThemeAmber\" /&gt; --&gt;\n" + 
			"    </li>\n" + 
			"    <li style=\"text-align: left;\">\n" + 
			"        <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"    </span>\n" + 
			"        &lt;!-- &lt;inherits name=\"gwt.material.design.themes.ThemeBrown\" /&gt; --&gt;\n" + 
			"    </li>\n" + 
			"</ol>\n" + 
			"<p style=\"text-align: center;\">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;!-- &lt;inherits name=\"gwt.material.design.themes.ThemeGreen\" /&gt; --&gt;\n" + 
			"</p>\n" + 
			"<p style=\"text-align: center;\">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;!-- &lt;inherits name=\"gwt.material.design.themes.ThemeGrey\" /&gt; --&gt;\n" + 
			"</p>\n" + 
			"<p style=\"text-align: justify;\">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;!-- &lt;inherits name=\"gwt.material.design.themes.ThemeOrange\" /&gt; --&gt;\n" + 
			"</p>\n" + 
			"<p style=\"text-align: justify;\">\n" + 
			"    <span class=\"Apple-tab-span\" style=\"white-space:pre\">\n" + 
			"</span>\n" + 
			"    &lt;!-- &lt;inherits name=\"gwt.material.design.themes.ThemePink\" /&gt; --&gt;\n" + 
			"</p>\n" + 
			"<p style=\"text-align: left; \">\n" + 
			"    <br>\n" + 
			"</p>";
}
