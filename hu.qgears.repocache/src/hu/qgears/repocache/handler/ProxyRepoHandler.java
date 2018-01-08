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
import hu.qgears.repocache.https.HttpsProxyConnectionsManager;
import hu.qgears.repocache.https.HttpsProxyConnectionsManager.RegistryEntry;

public class ProxyRepoHandler extends MyRequestHandler {
	private static Log log=LogFactory.getLog(ProxyRepoHandler.class);
	private boolean updateProxyPort;

	public ProxyRepoHandler(RepoCache rc) {
		super(rc);
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String remoteHost=baseRequest.getRemoteHost();
		int remotePort=baseRequest.getRemotePort();
		RegistryEntry entry=null;
		if("127.0.0.1".equals(remoteHost))
		{
			entry=HttpsProxyConnectionsManager.getInstance().get(remotePort);
			if(entry!=null&&entry.closed)
			{
				entry=null;
			}
		}
		System.out.println("Channel: "+remoteHost+" "+remotePort);
		String servername=baseRequest.getServerName();
		String scheme="http";
		int port;
		boolean rwMode;
		if(entry!=null)
		{
			scheme="https";
			baseRequest.setScheme(scheme);
			servername=entry.servername;
			port=entry.port;
			rwMode=entry.rwMode;
		}else
		{
			port=baseRequest.getServerPort();
			rwMode=updateProxyPort;
		}
		StringBuilder bld=new StringBuilder();
		bld.append(servername);
		if(port!=getDefaultPort(scheme))
		{
			bld.append(":");
			bld.append(Integer.toString(port));
		}
		String pathStr="proxy/"+scheme+"/"+bld+baseRequest.getRequestURI();
		Path path = new Path(pathStr);
		ClientQuery q=new ClientQueryHttp(target, baseRequest, request, response, rc, path);
		log.debug("Proxy request: " + q.path+" "+(rwMode?"RW":"RO")+" HTTPS PROXY: "+entry);
		super.handleQlientQuery(q, baseRequest, response, rwMode);
		log.debug("Proxy request response status: " + response.getStatus() + ", type: " + response.getContentType());
	}
	private int getDefaultPort(String scheme) throws IOException {
		switch (scheme) {
		case "http":
			return 80;
		case "https":
			return 443;
		default:
			throw new IOException("Invalid scheme '"+scheme+"'");
		}
	}
	public ProxyRepoHandler setUpdateProxyPort(boolean updateProxyPort) {
		this.updateProxyPort = updateProxyPort;
		return this;
	}
}
