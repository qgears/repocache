package hu.qgears.repocache.mavenplugin;

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

/**
 * TODO maven repos are removed as a feature.
 */
public class RepoPluginMaven extends AbstractRepoPlugin
{
	private Log log=LogFactory.getLog(getClass());
	private RepoCache rc;
	public RepoPluginMaven(RepoCache rc) {
		this.rc=rc;
	}

	public String getPath() {
		return "maven";
	}
	public Map<String, String> getMavenRepos() {
		return new TreeMap<>(// TODO rc.getConfiguration().getMvnrepos()
				);
	}
	@Override
	public QueryResponse getOnlineResponse(Path fullPath, Path localPath, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		if(localPath.pieces.size()==0)
		{
			return new MavenListing(q, this).generate();
		}
		for (Map.Entry<String, String> entry : getMavenRepos().entrySet()) {
			if (localPath.pieces.get(0).equals(entry.getKey())) {
				Path ref = new Path(localPath).remove(0);
				String httpPath = entry.getValue() + ref.toStringPath();
				if(!netAllowed)
				{
					log.debug("Path not updated from remote server: "+httpPath+" local path: "+localPath);
					return null;
				}
				QueryResponse response = q.rc.client.get(new HttpGet(
						q.rc.createTmpFile(q.getPath()), httpPath, 
						rc.getConfiguration()));
				return response;
			}
		}
		return null;
	}
}
