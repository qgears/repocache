package hu.qgears.repocache;

import java.util.Map;

public class P2Listing extends AbstractPage
{
	RepoPluginP2 p2;
	public P2Listing(ClientQuery query, RepoPluginP2 p2) {
		super(query);
		this.p2=p2;
	}

	@Override
	protected void doGenerate() {
		folder=true;
		write("Index of P2 repositiory\n\n<a href=\"../\">Parent Directory</a><br/>\n<a href=\"compositeArtifacts.xml\">compositeArtifacts.xml</a><br/>\n<a href=\"compositeContent.xml\">compositeContent.xml</a><br/>\n");
		for(Map.Entry<String, RepoConfig> e: p2.getP2Repos().entrySet())
		{
			write("\t<a href=\"");
			writeValue(e.getKey());
			write("/\">");
			writeHtml(e.getKey());
			write("</a> - mirror of: ");
			writeHtml(e.getValue().getPrimaryHost()+e.getValue().getFile()+"/");
			write(" mirror site used: <a href=");
			writeValue(e.getValue().getBaseUrl());
			write(">");
			writeHtml(e.getValue().getBaseUrl());
			write("</a><br/>\n");
		}
	}

}
