package hu.qgears.repocache;

import java.io.File;

public class P2RepoVersionArtifacts extends AbstractPage {
	public static String file="compositeArtifacts.xml";
	private long timestamp;
	private RepoPluginP2 p2;
	private File f;

	public P2RepoVersionArtifacts(ClientQuery query, RepoPluginP2 p2, long timestamp, File f) {
		super(query);
		this.p2=p2;
		this.timestamp=timestamp;
		this.f = f;
	}
	
	@Override
	protected void doGenerate() {
		File[] files = f.listFiles();
		int cnt = 0;
		for (File dir : files) if (dir.isDirectory()) cnt++;
		write("<?xml version='1.0' encoding='UTF-8'?>\n<?compositeArtifactRepository version='1.0.0'?>\n<repository name='QGears p2 repo cache repository' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1'>\n  <properties size='1'>\n    <property name='p2.timestamp' value='");
		writeObject(timestamp);
		write("'/>\n  </properties>\n  <children size='");
		writeObject(cnt);
		write("'>\n");
		for (File dir : files) {
			if (dir.isDirectory()) {
				write("    <child location='");
				writeObject(dir.getName());
				write("'/>\n");
			}
		}
		write("  </children>\n</repository>\n");
	}

}
