package hu.qgears.repocache.config;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientSetup;

public class ConfigListing extends AbstractPage
{

	public ConfigListing(ClientQuery query) {
		super(query);
	}

	@Override
	protected void doGenerate() {
		printParameters();
		write("<script language=\"javascript\" type=\"text/javascript\">\nfunction addParameter(link, param){\n    link += param;\n    return link;\n}\n</script>\n\n<h1>Repo Cache configuration</h1>\n<a href=\"../\">../</a><br/>\n<a href=\"./\">reload</a><br/>\nLocal Only: ");
		writeObject(getQuery().rc.getConfiguration().getCommandLine().localOnly);
		write("<br/>\n<a href=\"config.xml\">config.xml</a><br/>\n<a href=\"repoModeConfig\\\">repoModeConfig</a><br/>\nClient IP address: ");
		writeObject(getQuery().getClientIdentifier());
		write("<br/>\n<h2>Current client setup</h2>\nUpdater clients, validity in minutes: <input id=\"validity\" name=\"validity\"/><br/>\n");
		for(EClientMode m: EClientMode.values())
		{
			write("<a href=\"setClientMode?client=");
			writeObject(getQuery().getClientIdentifier());
			write("&amp;mode=");
			writeObject(m);
			write("&amp;validInMinute=\" onclick=\"location.href=addParameter(this.href,document.getElementById('validity').value);return false;\">Set ");
			writeObject(getQuery().getClientIdentifier());
			write(" client to ");
			writeObject(m);
			write("</a><br/>\n");
		}
		if (!getQuery().rc.getConfiguration().getClientSetup(getQuery().getClientIdentifier()).isShawRealFolderListing()) {
			write("<a href=\"setClientMode?client=");
			writeObject(getQuery().getClientIdentifier());
			write("&amp;shawRealFolderListing=true\">Set ");
			writeObject(getQuery().getClientIdentifier());
			write(" client ENABLE real folder listing.</a><br/>\n");
		} else {
			write("<a href=\"setClientMode?client=");
			writeObject(getQuery().getClientIdentifier());
			write("&amp;shawRealFolderListing=false\">Set ");
			writeObject(getQuery().getClientIdentifier());
			write(" client DISABLE real folder listing.</a><br/>\n");
		}
		for(EClientMode m: EClientMode.values())
		{
			write("<a href=\"setClientMode?client=this&amp;mode=");
			writeObject(m);
			write("&amp;validInMinute=\" onclick=\"location.href=addParameter(this.href,document.getElementById('validity').value);return false;\">Set this client to ");
			writeObject(m);
			write("</a><br/>\n");
		}
		if (!getQuery().rc.getConfiguration().getClientSetup(getQuery().getClientIdentifier()).isShawRealFolderListing()) {
			write("<a href=\"setClientMode?client=this&amp;shawRealFolderListing=true\">Set this client ENABLE real folder listing.</a><br/>\n");
		} else {
			write("<a href=\"setClientMode?client=this&amp;shawRealFolderListing=false\">Set this client DISABLE real folder listing.</a><br/>\n");
		}
		write("<ul>\n");
		synchronized (getQuery().rc.getConfiguration().getClients()) {
			for(ClientSetup c: getQuery().rc.getConfiguration().getClients().values())
			{
				write("<li>");
				writeObject(c);
				write("</li>\n");
			}
		}
		write("<h2>Current staging area</h2>\n(Added but not committed changes.)\n<a href=\"commit\">Execute commit now</a>\n<a href=\"revert\">Execute revert</a>\n<pre>\n");
		writeHtml(getQuery().rc.getCommitTimer().getCurrentStagingMessage());
		write("\n</pre>\n");
	}
}
