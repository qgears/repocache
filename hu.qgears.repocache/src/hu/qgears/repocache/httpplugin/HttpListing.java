package hu.qgears.repocache.httpplugin;

import java.util.Map;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;

public class HttpListing extends AbstractPage
{
	RepoPluginHttp http;
	public HttpListing(ClientQuery query, RepoPluginHttp http) {
		super(query);
		this.http=http;
	}

	@Override
	protected void doGenerate() {
		folder=true;
		write("Index of HTTP repositiory\n\n<a href=\"../\">Parent Directory</a><br/>\n");
		for(Map.Entry<String, String> e: http.getHttpRepos().entrySet())
		{
			write("\t<a href=\"");
			writeValue(e.getKey());
			write("/\">");
			writeHtml(e.getKey());
			write("</a> - mirror of: ");
			writeHtml(e.getValue());
			write("<br/>\n");
		}
	}

}
