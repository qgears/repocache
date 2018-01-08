package hu.qgears.repocache.qpage;

import java.io.IOException;

import hu.qgears.repocache.utils.InMemoryPost;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;

public abstract class QComponent {

	protected QPage page;
	protected String id;
	

	public QComponent(QPage page, String id) {
		super();
		this.page = page;
		this.id = id;
		page.add(this);
	}

	abstract public void generateExampleHtmlObject(RAbstractTemplatePart parent);

	abstract public void init(RAbstractTemplatePart parent);

	abstract public void handle(RAbstractTemplatePart parent, InMemoryPost post) throws IOException;

	final public String getId() {
		return id;
	}

}
