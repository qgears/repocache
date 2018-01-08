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

public class RepoPluginHttp extends AbstractRepoPlugin
{
	private Log log=LogFactory.getLog(getClass());
	private RepoCache rc;

	public RepoPluginHttp(RepoCache rc) {
		this.rc=rc;
	}

	public String getPath() {
		return "http";
	}
	public Map<String, String> getHttpRepos() {
		return new TreeMap<>(rc.getConfiguration().getHttprepos());
	}
	@Override
	public QueryResponse getOnlineResponse(Path fullPath, Path localPath, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		if(localPath.pieces.size()==0)
		{
			return new HttpListing(q, this).generate();
		}
		for (Map.Entry<String, String> entry : rc.getConfiguration().getHttprepos().entrySet()) {
			if (localPath.pieces.get(0).equals(entry.getKey())) {
				Path ref = new Path(localPath).remove(0);
				String httpPath = entry.getValue() + ref.toStringPath();
				if(!netAllowed||noRefresh(ref, cachedContent))
				{
					log.debug("Path not updated from remote server: "+httpPath+" local path: "+localPath);
					return null;
				}
				QueryResponse response = q.rc.client.get(new HttpGet(q.rc.createTmpFile(q.getPath()), httpPath));
				return response;
			}
		}
		return null;
	}

	private boolean noRefresh(Path ref, QueryResponse cachedContent) {
		if(cachedContent!=null&&ref.getFileName().endsWith(".tar.gz"))
		{
			return true;
		}
		return false;
	}
}
