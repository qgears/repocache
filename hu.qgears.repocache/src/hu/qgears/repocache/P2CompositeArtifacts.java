package hu.qgears.repocache;

import java.util.Map;

public class P2CompositeArtifacts extends AbstractPage
{
	public static String file="compositeArtifacts.xml";
	private long timestamp;
	private RepoPluginP2 p2;

	public P2CompositeArtifacts(ClientQuery query, RepoPluginP2 p2, long timestamp) {
		super(query);
		this.p2=p2;
		this.timestamp=timestamp;
	}
	
	@Override
	protected void doGenerate() {
		Map<String, P2RepoConfig> m=p2.getP2Repos();
		write("<?xml version='1.0' encoding='UTF-8'?>\n<?compositeArtifactRepository version='1.0.0'?>\n<repository name='QGears p2 repo cache repository' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1'>\n  <properties size='1'>\n    <property name='p2.timestamp' value='");
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
		write("  </children>\n</repository>\n");
	}
}
