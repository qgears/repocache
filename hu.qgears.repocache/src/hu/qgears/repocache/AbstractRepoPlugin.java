package hu.qgears.repocache;

import java.io.IOException;

abstract public class AbstractRepoPlugin {

	abstract public String getPath();

	abstract public QueryResponse getOnlineResponse(Path remove, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException;

	abstract public boolean isUpdateModeNormal(String repoName);
	
	abstract public boolean createNewVersionOnRewriteMode(Path path);
	
}
