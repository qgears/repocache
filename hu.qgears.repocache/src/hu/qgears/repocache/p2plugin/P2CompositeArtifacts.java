package hu.qgears.repocache.p2plugin;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;

public class P2CompositeArtifacts extends AbstractPage
{
	private long timestamp;
	private String reponame;
	private int maxVersion;

	public P2CompositeArtifacts(ClientQuery query,int maxVersion, String reponame) {
		super(query);
		this.maxVersion = maxVersion;
		this.reponame = reponame;
		this.timestamp=System.currentTimeMillis();
	}
	
	@Override
	protected void doGenerate() {
		write("<?xml version='1.0' encoding='UTF-8'?>\n<?compositeArtifactRepository version='1.0.0'?>\n<repository name='");
		writeObject(reponame);
		write("' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1' >\n  <properties size='1'>\n    <property name='p2.timestamp' value='");
		writeObject(timestamp);
		write("' />\n  </properties>\n  <children size='");
		writeObject(maxVersion);
		write("'>\n");
		for(int i = 1; i<= maxVersion; i++)
		{
			write("    <child location='");
			writeObject(ReplaceP2Plugin.getVersionSubfolder(i));
			write("' />\n");
		}
		write("  </children>\n</repository>\n");
	}
}
