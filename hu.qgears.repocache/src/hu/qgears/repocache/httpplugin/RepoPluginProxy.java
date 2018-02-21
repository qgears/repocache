package hu.qgears.repocache.httpplugin;

import java.io.IOException;

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

	public RepoPluginProxy(RepoCache rc) {
	}

	public String getPath() {
		return "proxy";
	}
	@Override
	public QueryResponse getOnlineResponse(Path fullPath, Path localPath, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		if(localPath.pieces.size()<2)
		{
			return null;
		}
		String protocol=localPath.pieces.get(0);
		Path noProtocol=new Path(localPath).remove(0);
		String httpPath = protocol+"://"+noProtocol.toStringPath();
		if(!netAllowed)
		{
			log.debug("Path offline: "+localPath.toStringPath());
			return null;
		}
		log.debug("Path update: "+ localPath.toStringPath() +" remote url: "+httpPath);
		QueryResponse response = q.rc.client.get(new HttpGet(q.rc.createTmpFile(q.getPath()), 
				httpPath, q.rc.getConfiguration()));
		return response;
	}
}
