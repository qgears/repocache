package hu.qgears.repocache.handler;

import java.io.IOException;

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
		Path path = new Path("proxy/"+baseRequest.getServerName()+baseRequest.getRequestURI());
		ClientQuery q=new ClientQueryHttp(target, baseRequest, request, response, rc, path);
		log.info("Path for proxy request is: " + q.path + ", localPort: " + baseRequest.getLocalPort());
		super.handleQlientQuery(q, baseRequest, response);
	}


}
