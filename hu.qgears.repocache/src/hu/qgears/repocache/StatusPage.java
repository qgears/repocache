package hu.qgears.repocache;

public class StatusPage extends AbstractPage {
	
	public StatusPage(ClientQuery query) {
		super(query);
	}

	@Override
	protected void doGenerate() {
		folder=true;
		write("<h1>");
		writeHtml(getQuery().rc.getConfiguration().getCommandLine().name);
		write("</h1>\n\nVersion: ");
		writeObject(getQuery().rc.repoVersion);
		write("<br/><br/>\n\n<a href=\"config2.html\">New configuration</a><br/>\n<a href=\"config\">Configuration</a><br/>\nPlugins:\n<br/>\n<ul>\n");
		for(AbstractRepoPlugin plugin: getQuery().rc.getPlugins())
		{
			write("\t<li><a href=\"");
			writeValue(plugin.getPath());
			write("/\">");
			writeHtml(plugin.getPath());
			write("</a></li>\n");
		}
		write("</ul>\n<a href=\"https://github.com/qgears/repocache\">Repo Cache project page</a>\n");
	}
}
