package hu.qgears.repocache.handler;

import hu.qgears.repocache.AbstractHTMLPage;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.httpplugin.RepoPluginProxy;

public class StatusPage extends AbstractHTMLPage {
	
	public StatusPage(ClientQuery query) {
		super(query);
	}

	@Override
	protected void writeHTMLBody() {
		VersionMetadata vm = VersionMetadata.get();
		folder=true;
		write("<h1>");
		writeHtml(getQuery().rc.getConfiguration().getName());
		write("</h1>\n\n<a href=\"config.html\">Configuration</a><br/>\n<a href=\"access-log.html\">Access log</a><br/>\nContent:\n<br/>\n<ul>\n");
		RepoPluginProxy plugin=getQuery().rc.plugin;
		write("\t<li><a href=\"");
		writeValue(plugin.getPath());
		write("/\">");
		writeHtml(plugin.getPath());
		write("</a></li>\n</ul>\n\nPowered by: <a href=\"");
		writeObject(vm.getGitHubUrl());
		write("\">Repo Cache by Q-Gears Kft. version ");
		writeObject(vm.getVersion());
		write("</a>\n");
	}

	@Override
	protected String getTitleFragment() {
		return getQuery().rc.getConfiguration().getName()+"";
	}

}
