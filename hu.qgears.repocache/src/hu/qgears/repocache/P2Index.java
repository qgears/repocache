package hu.qgears.repocache;

public class P2Index extends AbstractPage {

	public P2Index(ClientQuery query) {
		super(query);
	}

	@Override
	protected void doGenerate() {
		write("version=1\nmetadata.repository.factory.order=compositeContent.xml,\\!\nartifact.repository.factory.order=compositeArtifacts.xml,\\!\n");
	}

}
