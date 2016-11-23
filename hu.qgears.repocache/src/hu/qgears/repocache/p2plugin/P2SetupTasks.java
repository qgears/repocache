package hu.qgears.repocache.p2plugin;

import java.util.Map;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;

/**
 * Generator that generates setup code for Ooomph to use this repository
 * instead of all mirrored repos.
 * TODO finish
 */
public class P2SetupTasks extends AbstractPage
{
	private RepoPluginP2 p2;
	public P2SetupTasks(ClientQuery query, RepoPluginP2 p2) {
		super(query);
		this.p2=p2;
	}

	@Override
	protected void doGenerate() {
		for(Map.Entry<String, P2RepoConfig> entry: p2.getP2Repos().entrySet())
		{
			write("  <setupTask\n      xsi:type=\"setup:RedirectionTask\"\n      id=\"Redirect_Neon_To_Mirror\"\n\t\t\tsourceURL=\"http://download.eclipse.org/releases/neon/201610111000\"\n      targetURL=\"http://localhost:8080/p2/eclipse-release-neon-201610111000/\">\n    <description>Redirect all requests to: #X# to this mirror.</description>\n  </setupTask>\n");
		}
		write("  <setupTask\n      xsi:type=\"setup:RedirectionTask\"\n      id=\"Redirect_Neon_To_Mirror\"\n\t\t\tsourceURL=\"http://download.eclipse.org/releases/neon/201610111000\"\n      targetURL=\"http://localhost:8080/p2/eclipse-release-neon-201610111000/\">\n    <description>Redirect all requests of Mars releases to internal Mirror</description>\n  </setupTask>\n  <setupTask\n      xsi:type=\"setup:RedirectionTask\"\n      id=\"Redirect_OOMPH_To_Mirror\"\n      sourceURL=\"http://download.eclipse.org/oomph/updates/latest\"\n      targetURL=\"http://localhost:8080/p2/oomph-updates-milestone-latest/\">\n    <description>Redirect all requests of OOMPH to internal Mirror</description>\n  </setupTask>\n  <setupTask\n      xsi:type=\"setup:RedirectionTask\"\n      id=\"Redirect_OOMPH_To_Mirror\"\n      sourceURL=\"http://download.eclipse.org/technology/epp/packages/neon\"\n      targetURL=\"http://localhost:8080/p2/eclipse-neon/\">\n    <description>Redirect all requests of Neon releases to internal Mirror</description>\n  </setupTask>\n");
	}

}
