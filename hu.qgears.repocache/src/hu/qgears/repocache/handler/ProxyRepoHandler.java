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
	private boolean https=false;
	
	public ProxyRepoHandler(RepoCache rc) {
		super(rc);
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if(https)
		{
			baseRequest.setScheme("https");
		}
		String servername=baseRequest.getServerName();
		StringBuilder bld=new StringBuilder();
		bld.append(servername);
		int port=baseRequest.getServerPort();
		if(port!=getDefaultPort())
		{
			bld.append(":");
			bld.append(Integer.toString(port));
		}
		String pathStr="proxy/"+getProtocol()+"/"+baseRequest.getServerName()+baseRequest.getRequestURI();
		log.debug("Proxy request arrived to port: "+baseRequest.getLocalPort()+" target URL: "+pathStr);
		Path path = new Path(pathStr);
		ClientQuery q=new ClientQueryHttp(target, baseRequest, request, response, rc, path);
		log.debug("Path for proxy request is: " + q.path + ", localPort: " + baseRequest.getLocalPort());
		super.handleQlientQuery(q, baseRequest, response);
		log.debug("Proxy request response status: " + response.getStatus() + ", type: " + response.getContentType());
	}
	private int getDefaultPort() {
		return https?443:80;
	}
	private String getProtocol() {
		return https?"https":"http";
	}
	public ProxyRepoHandler setUpdateProxyPort(boolean updateProxyPort) {
		this.updateProxyPort = updateProxyPort;
		return this;
	}
	public ProxyRepoHandler setHttps(boolean b) {
		this.https=b;
		return this;
	}

}
