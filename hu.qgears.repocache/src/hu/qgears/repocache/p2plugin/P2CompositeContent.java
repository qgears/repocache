package hu.qgears.repocache.p2plugin;

import java.util.Map;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;

public class P2CompositeContent extends AbstractPage
{
	private RepoPluginP2 p2;
	private long timestamp;
	public static String file="compositeContent.xml";

	public P2CompositeContent(ClientQuery query, RepoPluginP2 p2, long timestamp) {
		super(query);
		this.p2=p2;
		this.timestamp=timestamp;
	}
	
	@Override
	protected void doGenerate() {
		Map<String, P2RepoConfig> m=p2.getP2Repos();
		write("<?xml version='1.0' encoding='UTF-8'?>\n<?compositeMetadataRepository version='1.0.0'?>\n<repository name='QGears p2 repo cache repository' type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1'>\n  <properties size='1'>\n    <property name='p2.timestamp' value='");
		writeObject(timestamp);
		write("'/>\n  </properties>\n  <children size='");
		writeObject(m.size());
		write("'>\n");
		for(Map.Entry<String, P2RepoConfig> entry: m.entrySet())
		{
			write("    <child location='");
			writeObject(entry.getKey());
			write("'/>\n");
		}
		write("  </children>\n</repository>\n\n");
	}
}
