package hu.qgears.repocache.handler;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Request;

import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientQueryHttp;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.RepoCache;

public class ProxyRepoHandler extends MyRequestHandler {
	private static Log log=LogFactory.getLog(ProxyRepoHandler.class);
	
	public ProxyRepoHandler(RepoCache rc) {
		super(rc);
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		log.info("Proxy request arrived, URL: " + baseRequest.getRequestURL());
		String replacedUrl = replaceMirrorUrl(baseRequest.getRequestURL().toString());
		Path path = new Path("proxy/http/"+replacedUrl);
		ClientQuery q=new ClientQueryHttp(target, baseRequest, request, response, rc, path);
		log.info("Path for proxy request is: " + q.path + ", localPort: " + baseRequest.getLocalPort());
		super.handleQlientQuery(q, baseRequest, response);
		log.info("Proxy request response status: " + response.getStatus() + ", type: " + response.getContentType());
	}

	private String replaceMirrorUrl (String baseUrl) {
		String replacedUrl = baseUrl;
		Map<String, String> urls = rc.getConfiguration().getProxyrepos();
		for (String url : urls.keySet()) {
			if (baseUrl.startsWith(url)) {
				replacedUrl = urls.get(url) + baseUrl.substring(url.length());
				break;
			}
		}
		if (replacedUrl.startsWith("http://")) replacedUrl = replacedUrl.substring(7);
		return replacedUrl;
	}
}
