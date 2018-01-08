package hu.qgears.repocache;

import java.io.IOException;

abstract public class AbstractRepoPlugin {

	/**
	 * 
	 * @param fullPath Full path in the storage repsitory
	 * @param pluginLocalPath Local path within the plugin
	 * @param q
	 * @param cachedContent
	 * @param netAllowed
	 * @return
	 * @throws IOException
	 */
	abstract public QueryResponse getOnlineResponse(Path fullPath, Path pluginLocalPath, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException;

}
