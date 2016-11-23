package hu.qgears.repocache;

import java.util.List;

public class P2RepoVersionListing extends AbstractPage {
	RepoPluginP2 p2;
	String p2Repo;
	
	public P2RepoVersionListing(ClientQuery query, RepoPluginP2 p2, String p2Repo) {
		super(query);
		this.p2=p2;
		this.p2Repo=p2Repo;
	}

	@Override
	protected void doGenerate() {
		List<String> folderList = P2VersionFolderUtil.getInstance().listFolders(p2Repo);
		folder=true;
		write("Index of P2 repositiory\n\n<a href=\"../\">Parent Directory</a><br/>\n<a href=\"compositeArtifacts.xml\">compositeArtifacts.xml</a><br/>\n<a href=\"compositeContent.xml\">compositeContent.xml</a><br/>\n");
		for (String dir : folderList) {
			write("\t<a href=\"");
			writeValue(dir);
			write("/\">");
			writeHtml(dir);
			write("</a><br/>\n");
		}
	}


}
