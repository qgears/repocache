package hu.qgears.repocache.p2plugin;

import java.util.Arrays;
import java.util.List;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;

public class P2RepoVersionArtifacts extends AbstractPage {
	public static String file="compositeArtifacts.xml";
	public static List<String> fileNames= Arrays.asList("compositeArtifacts.xml", "artifacts.xml", "artifacts.jar", "artifacts.xml.xz", "compositeArtifacts.jar");
	private long timestamp;
	private String p2Repo;

	public P2RepoVersionArtifacts(ClientQuery query, long timestamp,String p2Repo) {
		super(query);
		this.timestamp=timestamp;
		this.p2Repo = p2Repo;
	}
	
	@Override
	protected void doGenerate() {
		List<String> folderList = P2VersionFolderUtil.getInstance().listFolders(p2Repo);
		write("<?xml version='1.0' encoding='UTF-8'?>\n<?compositeArtifactRepository version='1.0.0'?>\n<repository name='QGears p2 repo cache repository' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1'>\n  <properties size='1'>\n    <property name='p2.timestamp' value='");
		writeObject(timestamp);
		write("'/>\n  </properties>\n  <children size='");
		writeObject(folderList.size());
		write("'>\n");
		for (String dir : folderList) {
			write("    <child location='");
			writeObject(dir);
			write("'/>\n");
		}
		write("  </children>\n</repository>\n");
	}

}
