package hu.qgears.repocache;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import hu.qgears.quickjs.qpage.HtmlTemplate;

abstract public class AbstractPage extends HtmlTemplate
{
	protected boolean folder;
	private ClientQuery query;
	public AbstractPage(ClientQuery query)
	{
		super(new StringWriter());
		this.query=query;
	}
	public ClientQuery getQuery()
	{
		return query;
	}
	public QueryResponse generate() throws IOException {
		doGenerate();
		return new QueryResponseByteArray("", out.toString().getBytes(
				StandardCharsets.UTF_8), folder).setContentType("text/html; charset=utf-8");
	}
	
	/**
	 * Emits the textual reply body, such as a HTML or XML document.  
	 */
	abstract protected void doGenerate() throws IOException;
	
	protected void printParameters() {
		for(String s: getQuery().getParameterNames())
		{
			writeObject(s);
			write(":");
			String[]vs=getQuery().getParameterValues(s);
			for(String v: vs)
			{
				writeObject(v);
			}
			write("<br/>\n");
		}
	}

}
