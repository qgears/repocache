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
	public QueryResponse getOnlineResponse(Path fullPath, Path localPath, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		if(localPath.pieces.size()<2)
		{
			return null;
		}
		String protocol=localPath.pieces.get(0);
		Path noProtocol=new Path(localPath).remove(0);
		String httpPath = replaceMirrorUrl(protocol+"://"+noProtocol.toStringPath());
		if(!netAllowed)
		{
			log.debug("Path offline: "+localPath.toStringPath());
			return null;
		}
		log.debug("Path update: "+ localPath.toStringPath() +" remote url: "+httpPath);
		QueryResponse response = q.rc.client.get(new HttpGet(q.rc.createTmpFile(q.getPath()), httpPath));
		return response;
	}

	private String replaceMirrorUrl (String baseUrl) {
		String replacedUrl = baseUrl;
		Map<String, String> urls = rc.getConfiguration().getProxyrepos();
		int usedLength = 0;
		for (String url : urls.keySet()) {
			// Replace with the most specific (=longest) matching url
			if (baseUrl.startsWith(url) && url.length()>usedLength) {
				usedLength = url.length();
				replacedUrl = urls.get(url) + baseUrl.substring(url.length());
				log.info("Mirror URL replacement done: "+baseUrl+" -> "+replacedUrl);
			}
		}
		return replacedUrl;
	}
}
