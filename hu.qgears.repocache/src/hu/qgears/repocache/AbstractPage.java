package hu.qgears.repocache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;

abstract public class AbstractPage extends RAbstractTemplatePart
{
	protected boolean folder;
	public AbstractPage(ClientQuery query)
	{
		super(query);
	}
	public ClientQuery getQuery()
	{
		return (ClientQuery) getCodeGeneratorContext();
	}
	public QueryResponse generate() throws IOException {
		doGenerate();
		finishDeferredParts();
		return new QueryResponse("", getTemplateState().getOut().toString().getBytes(StandardCharsets.UTF_8), folder);
	}
	abstract protected void doGenerate();
	protected void writeHtml(String value) {
		// TODO Auto-generated method stub
		writeObject(value);
	}

	protected void writeValue(String key) {
		// TODO Auto-generated method stub
		writeObject(key);
		
	}
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
