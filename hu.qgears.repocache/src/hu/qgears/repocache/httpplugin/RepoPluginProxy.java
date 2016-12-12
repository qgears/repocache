package hu.qgears.repocache.httpplugin;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.repocache.AbstractRepoPlugin;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.RepoCache;
import hu.qgears.repocache.httpget.HttpGet;

public class RepoPluginProxy extends AbstractRepoPlugin {
	private Log log=LogFactory.getLog(getClass());
	private RepoCache rc;

	public RepoPluginProxy(RepoCache rc) {
		this.rc=rc;
	}

	public String getPath() {
		return "proxy";
	}
	public Map<String, String> getHttpRepos() {
		return new TreeMap<>(rc.getConfiguration().getHttprepos());
	}
	@Override
	public QueryResponse getOnlineResponse(Path localPath, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		if(localPath.pieces.size()<2)
		{
			return null;
		}
		Path httpPath2 = new Path(localPath).remove(0);
		String httpPath = "http://" + httpPath2.toStringPath();
		if(!netAllowed)
		{
			log.info("Path not updated from remote server: "+ httpPath +" local path: "+localPath);
			return null;
		}
		log.info("Update from remote server: "+ httpPath +" local path: "+localPath);
		QueryResponse response = q.rc.client.get(new HttpGet(q.rc.createTmpFile(q.path), httpPath));
		return response;
	}

}
