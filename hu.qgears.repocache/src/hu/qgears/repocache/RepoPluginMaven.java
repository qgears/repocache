package hu.qgears.repocache;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.repocache.httpget.HttpGet;

public class RepoPluginMaven extends AbstractRepoPlugin
{
	private Logger log=LoggerFactory.getLogger(getClass());
	private RepoCache rc;
	public RepoPluginMaven(RepoCache rc) {
		this.rc=rc;
	}

	public String getPath() {
		return "maven";
	}
	public Map<String, String> getMavenRepos() {
		return new TreeMap<>(rc.getConfiguration().getMvnrepos());
	}
	@Override
	public QueryResponse getOnlineResponse(Path localPath, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		if(localPath.pieces.size()==0)
		{
			return new MavenListing(q, this).generate();
		}
		for (Map.Entry<String, String> entry : rc.getConfiguration().getMvnrepos().entrySet()) {
			if (localPath.pieces.get(0).equals(entry.getKey())) {
				Path ref = new Path(localPath).remove(0);
				String httpPath = entry.getValue() + ref.toStringPath();
				if(!netAllowed)
				{
					log.info("Path not updated from remote server: "+httpPath+" local path: "+localPath);
					return null;
				}
				QueryResponse response = q.rc.client.get(new HttpGet(httpPath));
				return response;
			}
		}
		return null;
	}
}
