package hu.qgears.repocache;

public class StatusPage extends AbstractPage {
	
	public StatusPage(ClientQuery query) {
		super(query);
	}

	@Override
	protected void doGenerate() {
		folder=true;
		write("<h1>Maven and P2 repo cache</h1>\nPlugins:\n<br/>\n<ul>\n");
		for(AbstractRepoPlugin plugin: getQuery().rc.getPlugins())
		{
			write("\t<li><a href=\"");
			writeValue(plugin.getPath());
			write("/\">");
			writeHtml(plugin.getPath());
			write("</a></li>\n");
		}
		write("</ul>\n");
	}
}
