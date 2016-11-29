package hu.qgears.repocache.p2plugin;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;

public class P2Index extends AbstractPage {

	public static String filename = "p2.index";
	
	public P2Index(ClientQuery query) {
		super(query);
	}

	@Override
	protected void doGenerate() {
		write("version=1\nmetadata.repository.factory.order=compositeContent.xml,\\!\nartifact.repository.factory.order=compositeArtifacts.xml,\\!\n");
	}

}
