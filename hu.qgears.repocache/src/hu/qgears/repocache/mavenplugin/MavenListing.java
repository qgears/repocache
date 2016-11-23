package hu.qgears.repocache.mavenplugin;

import java.util.Map;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;

public class MavenListing extends AbstractPage
{
	RepoPluginMaven maven;
	public MavenListing(ClientQuery query, RepoPluginMaven maven) {
		super(query);
		this.maven=maven;
	}

	@Override
	protected void doGenerate() {
		folder=true;
		write("Index of MAVEN repositiories\n\n<a href=\"../\">Parent Directory</a><br/>\n");
		for(Map.Entry<String, String> e: maven.getMavenRepos().entrySet())
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
