package hu.qgears.repocache;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.qpage.QPageManager;
import hu.qgears.quickjs.utils.HttpSessionQPageManager;

abstract public class AbstractQPage extends AbstractHTMLPage
{
	protected QPage page;

	public AbstractQPage(ClientQuery query) {
		super(query);
	}
	abstract protected void initPage();
	
	@Override
	protected void writeHtmlHeaders() {
		super.writeHtmlHeaders();
		page.writeHeaders(this);
	}

	@Override
	protected void doGenerate() throws IOException {
 		HttpSession sess=getHttpQuery().baseRequest.getSession();
		QPageManager qpm=HttpSessionQPageManager.getManager(sess);
		page=qpm.getPage(getHttpQuery().baseRequest.getParameter(QPage.class.getSimpleName()));
		if(page!=null)
		{
			page.handle(this, getHttpQuery().getInMemoryPost());
			return;
		}else
		{
			page=new QPage(qpm);
			initPage();
			super.doGenerate();
		}
	}

	
	protected ClientQueryHttp getHttpQuery()
	{
		return (ClientQueryHttp)getQuery();
	}
	public void handle() throws IOException {
		QueryResponse resp=generate();
		resp.contentType="text/html; utf-8";
		getQuery().reply(resp);
	}

}
