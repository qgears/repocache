package hu.qgears.repocache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepoPluginHttp extends AbstractRepoPlugin
{
	private Logger log=LoggerFactory.getLogger(getClass());
	private Map<String, String> httprepos = new HashMap<>();
	public RepoPluginHttp() {
		// TODO parameter aliases
		httprepos.put("oomphneon", "http://mirror.switch.ch/eclipse/oomph/epp/neon/");
	}

	public String getPath() {
		return "http";
	}
	public Map<String, String> getP2Repos() {
		return new TreeMap<>(httprepos);
	}
	@Override
	public QueryResponse getOnlineResponse(Path localPath, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		if(localPath.pieces.size()==0)
		{
			return new HttpsListing(q, this).generate();
		}
		for (Map.Entry<String, String> entry : httprepos.entrySet()) {
			if (localPath.pieces.get(0).equals(entry.getKey())) {
				Path ref = new Path(localPath).remove(0);
				String httpPath = entry.getValue() + ref.toStringPath();
				if(!netAllowed||noRefresh(ref, cachedContent))
				{
					log.info("Path not updated from remote server: "+httpPath+" local path: "+localPath);
					return null;
				}
				QueryResponse response = q.rc.client.get(httpPath);
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
