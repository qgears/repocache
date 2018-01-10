package hu.qgears.repocache.qpage;

import java.io.IOException;
import java.io.Writer;

import hu.qgears.repocache.utils.InMemoryPost;

public abstract class QComponent extends HtmlTemplate
{

	protected QPage page;
	protected String id;
	

	public QComponent(QPage page, String id) {
		super((Writer) null);
		this.page = page;
		this.id = id;
		page.add(this);
	}

	abstract public void generateExampleHtmlObject(HtmlTemplate parent);

	abstract public void init(HtmlTemplate parent);

	abstract public void handle(HtmlTemplate parent, InMemoryPost post) throws IOException;

	final public String getId() {
		return id;
	}
	public QPage getPage() {
		return page;
	}

}
