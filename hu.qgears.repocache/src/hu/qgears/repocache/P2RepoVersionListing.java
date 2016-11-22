package hu.qgears.repocache;

import java.io.File;

public class P2RepoVersionListing extends AbstractPage {
	RepoPluginP2 p2;
	File f;
	
	public P2RepoVersionListing(ClientQuery query, RepoPluginP2 p2, File f) {
		super(query);
		this.p2=p2;
		this.f=f;
	}

	@Override
	protected void doGenerate() {
		folder=true;
		write("Index of P2 repositiory\n\n<a href=\"../\">Parent Directory</a><br/>\n<a href=\"compositeArtifacts.xml\">compositeArtifacts.xml</a><br/>\n<a href=\"compositeContent.xml\">compositeContent.xml</a><br/>\n");
		File[] files = f.listFiles();
		for (File dir : files) {
			if (dir.isDirectory()) {
				write("\t<a href=\"");
				writeValue(dir.getName());
				write("/\">");
				writeHtml(dir.getName());
				write("</a><br/>\n");
			}
		}
	}


}
