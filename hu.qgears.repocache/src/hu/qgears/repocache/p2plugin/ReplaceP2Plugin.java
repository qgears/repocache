package hu.qgears.repocache.p2plugin;

import java.io.IOException;

import hu.qgears.repocache.AbstractRepoPluginSubTree;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.config.AccessRules.PluginDef;

public class ReplaceP2Plugin extends AbstractRepoPluginSubTree
{

	@Override
	public void init(PluginDef pd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public QueryResponse getOnlineResponse(Path fullPath, Path pluginLocalPath, ClientQuery q,
			QueryResponse cachedContent, boolean netAllowed) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
