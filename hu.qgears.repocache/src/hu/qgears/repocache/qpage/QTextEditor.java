package hu.qgears.repocache.qpage;

import java.io.IOException;

import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.repocache.utils.InMemoryPost;

public class QTextEditor extends QComponent
{
	public final UtilListenableProperty<String> text=new UtilListenableProperty<>();
	public QTextEditor(QPage page, String identifier) {
		super(page, identifier);
	}
	
	public static void generateHeader(HtmlTemplate parent)
	{
		new HtmlTemplate(parent){

			public void generate() {
				write("<style>\n.dirty\n{\nbackground-color: lightblue;\n}\n.error\n{\nbackground-color: red;\n}\n</style>\n<script language=\"javascript\" type=\"text/javascript\">\nclass QTextEditor extends QComponent\n{\n\taddDomListeners()\n\t{\n\t\tthis.dom.oninput=this.oninput.bind(this);\n\t}\n\toninput()\n\t{\n\t\tthis.dom.className=\"dirty\";\n\t\tvar fd=this.page.createFormData(this);\n\t\tfd.append(\"text\", this.dom.value);\n\t\tthis.page.send(fd);\n\t}\n\tinitValue(text)\n\t{\n\t\tthis.dom.value=text;\n\t}\n}\n</script>\n");
			}
			
		}.generate();
	}

	public void generateExampleHtmlObject(HtmlTemplate parent) {
		new HtmlTemplate(parent){

			public void generate() {
				write("<textarea id=\"");
				writeObject(id);
				write("\" rows=\"4\" cols=\"50\"></textarea>\n");
			}
			
		}.generate();		
	}

	public void handle(HtmlTemplate parent, InMemoryPost post) throws IOException {
		text.setProperty(post.getParameter("text"));
	}

	@Override
	public void init(HtmlTemplate parent) {
		new HtmlTemplate(parent)
		{

			public void generate() {
				write("\tnew QTextEditor(page, \"");
				writeObject(id);
				write("\").initValue(\"");
				writeJSValue(text.getProperty());
				write("\");\n");
			}
			
		}.generate();
	}
}
