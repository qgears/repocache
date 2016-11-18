package hu.qgears.repocache.config;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientSetup;
import hu.qgears.repocache.EClientMode;

public class ConfigListing extends AbstractPage
{

	public ConfigListing(ClientQuery query) {
		super(query);
	}

	@Override
	protected void doGenerate() {
		printParameters();
//		handleConfigUpdates();
		write("<h1>Repo Cache configuration</h1>\n<a href=\"../\">../</a><br/>\n<a href=\"./\">reload</a><br/>\nLocal Only: ");
		writeObject(getQuery().rc.getConfiguration().getCommandLine().localOnly);
		write("<br/>\n<a href=\"config.xml\">config.xml</a><br/>\nClient IP address: ");
		writeObject(getQuery().getClientIdentifier());
		write("<br/>\n<h2>Current client setup</h2>\nUpdater clients:<br/>\n");
		for(EClientMode m: EClientMode.values())
		{
			write("<a href=\"setClientMode?client=");
			writeObject(getQuery().getClientIdentifier());
			write("&amp;mode=");
			writeObject(m);
			write("\">Set ");
			writeObject(getQuery().getClientIdentifier());
			write(" client to ");
			writeObject(m);
			write("</a><br/>\n");
		}
		for(EClientMode m: EClientMode.values())
		{
			write("<a href=\"setClientMode?client=this&amp;mode=");
			writeObject(m);
			write("\">Set this client to ");
			writeObject(m);
			write("</a><br/>\n");
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
	}
}
