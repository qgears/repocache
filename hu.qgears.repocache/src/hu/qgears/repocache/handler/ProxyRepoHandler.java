package hu.qgears.repocache.handler;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Request;

import hu.qgears.commons.UtilString;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientQueryHttp;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.RepoCache;
import hu.qgears.repocache.https.RewriteOutputStreamFilter;

public class ProxyRepoHandler extends MyRequestHandler {
	private static Log log=LogFactory.getLog(ProxyRepoHandler.class);
	private boolean updateProxyPort;

	public ProxyRepoHandler(RepoCache rc) {
		super(rc);
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String httpsProxyHeader=baseRequest.getHeader(RewriteOutputStreamFilter.headerName);
		String servername=baseRequest.getServerName();
		String scheme="http";
		int port;
		boolean rwMode;
		if(httpsProxyHeader!=null)
		{
			List<String> pieces=UtilString.split(httpsProxyHeader, " ");
			scheme=pieces.get(0);
			baseRequest.setScheme(scheme);
			servername=pieces.get(1);
			port=Integer.parseInt(pieces.get(2));
			switch(pieces.get(3))
			{
			case "r":
				rwMode=false;
				break;
			case "rw":
				rwMode=true;
				break;
			default:
				throw new IOException("Invalid rw mode: '"+pieces.get(3)+"'");
			}
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
		String pathStr="proxy/"+scheme+"/"+servername+baseRequest.getRequestURI();
		log.debug("Proxy request arrived : "+" target URL: "+pathStr+" "+(rwMode?"RW":"RO"));
		Path path = new Path(pathStr);
		ClientQuery q=new ClientQueryHttp(target, baseRequest, request, response, rc, path);
		log.debug("Path for proxy request is: " + q.path + ", localPort: " + baseRequest.getLocalPort());
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
