package hu.qgears.repocache;

import java.io.IOException;

public abstract class AbstractHTMLPage extends AbstractPage {

	public AbstractHTMLPage(ClientQuery query) {
		super(query);
	}

	/**
	 * 
	 * @return a text fragment, which will be included in the {@code <title>} 
	 * HTML tag, and should identify a page, conveniently for the user 
	 */
	protected abstract String getTitleFragment();
	
	/**
	 * Writes Javascript code that generates the HTMLM title tag.
	 */
	protected void writeHTMLTitle() {
		write("<script language=\"javascript\" type=\"text/javascript\">\n\tdocument.write(\"<title>[RepoCache] ");
		writeObject(getTitleFragment());
		write(" @ \" + window.location.host + \"</title>\");\n</script>\n");
	}
	
	protected void writeHTMLBody() {
		
	}
	
	@Override
	protected void doGenerate() throws IOException {
		write("<!DOCTYPE html>\n<html>\n<head>\n");
		writeHTMLTitle();
		writeHtmlHeaders();
		write("</head>\n<body>\n");
		writeHTMLBody();
		write("</body>\n</html>\n");
	}

	protected void writeHtmlHeaders() {
		// TODO Auto-generated method stub
		
	}
}
