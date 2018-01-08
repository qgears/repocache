package hu.qgears.repocache.qpage;

import java.io.IOException;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.repocache.utils.InMemoryPost;

public class QLabel extends QComponent
{
	public final UtilListenableProperty<String> text=new UtilListenableProperty<>();
	public QLabel(QPage page, String identifier) {
		super(page, identifier);
		text.getPropertyChangedEvent().addListener(new UtilEventListener<String>() {
			
			@Override
			public void eventHappened(String msg) {
				textChanged(msg);
			}
		});
	}

	public static void generateHeader(HtmlTemplate parent)
	{
		new HtmlTemplate(parent){

			public void generate() {
				write("<script language=\"javascript\" type=\"text/javascript\">\nclass QLabel extends QComponent\n{\n\taddDomListeners()\n\t{\n\t}\n\tinitValue(text)\n\t{\n\t\tthis.dom.innerHTML=text;\n\t}\n}\n</script>\n");
			}
			
		}.generate();
	}

	public void generateExampleHtmlObject(HtmlTemplate parent) {
		new HtmlTemplate(parent){

			public void generate() {
				write("<div id=\"");
				writeObject(id);
				write("\"></div>\n");
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
				write("\tnew QLabel(page, \"");
				writeObject(id);
				write("\").initValue(\"");
				writeJSValue(text.getProperty());
				write("\");\n");
			}
			
		}.generate();
	}
	protected void textChanged(final String msg) {
		if(page.inited)
		{
			new ChangeTemplate(page.getCurrentTemplate()){
				public void generate() {
					write("page.components['");
					writeJSValue(id);
					write("'].initValue(\"");
					writeJSValue(msg);
					write("\");\n");
				}
			}.generate();
		}
	}
}
