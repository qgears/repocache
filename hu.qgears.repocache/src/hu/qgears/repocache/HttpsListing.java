package hu.qgears.repocache;

import java.util.Map;

public class HttpsListing extends AbstractPage
{
	RepoPluginHttp http;
	public HttpsListing(ClientQuery query, RepoPluginHttp http) {
		super(query);
		this.http=http;
	}

	@Override
	protected void doGenerate() {
		folder=true;
		write("Index of HTTP repositiory\n\n<a href=\"../\">Parent Directory</a><br/>\n");
		for(Map.Entry<String, String> e: http.getP2Repos().entrySet())
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
