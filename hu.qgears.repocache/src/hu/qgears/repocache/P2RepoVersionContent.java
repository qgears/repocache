package hu.qgears.repocache;

import java.util.List;

public class P2RepoVersionContent extends AbstractPage {
	private RepoPluginP2 p2;
	private long timestamp;
	public static String file="compositeContent.xml";
	private String p2Repo;

	public P2RepoVersionContent(ClientQuery query, RepoPluginP2 p2, long timestamp, String p2Repo) {
		super(query);
		this.p2=p2;
		this.timestamp=timestamp;
		this.p2Repo = p2Repo;
	}
	
	@Override
	protected void doGenerate() {
		List<String> folderList = P2VersionFolderUtil.getInstance().listFolders(p2Repo);
		write("<?xml version='1.0' encoding='UTF-8'?>\n<?compositeMetadataRepository version='1.0.0'?>\n<repository name='QGears p2 repo cache repository' type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1'>\n  <properties size='1'>\n    <property name='p2.timestamp' value='");
		writeObject(timestamp);
		write("'/>\n  </properties>\n  <children size='");
		writeObject(folderList.size());
		write("'>\n");
		for (String dir : folderList) {
			write("    <child location='");
			writeObject(dir);
			write("'/>\n");
		}
		write("  </children>\n</repository>\n\n");
	}

}
