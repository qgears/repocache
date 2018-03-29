package hu.qgears.repocache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class DispatchByPortHandler extends AbstractHandler
{
	private static Log log = LogFactory.getLog(DispatchByPortHandler.class);
	private final Map<ServerConnector, Handler> handlerByConnector=new HashMap<>();
	private final RepoCache repoCache;
		
	public DispatchByPortHandler(final RepoCache repoCache) {
		this.repoCache = repoCache;
	}
	
	@Override
	public void handle(String target, Request baseRequest, 
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		final Connector c = baseRequest.getHttpChannel().getConnector();
		final String proxyConnHeader = baseRequest.getHeader(
				HttpHeader.PROXY_CONNECTION.asString());
		final int localPort = request.getLocalPort();
		
		if (localPort == repoCache.getPort() && proxyConnHeader != null) {
			log.error("Returning 'Bad request (400)', as the client sent a "
					+ "proxy request to the web UI port.\n" + baseRequest);
			// Returning HTTP error 400 if a proxy request comes to the web UI port
			response.sendError(HttpStatus.BAD_REQUEST_400, "Cannot serve proxy "
					+ "request on the WEB ui port");
		} else {
			final Handler h = handlerByConnector.get(c);
			
			if (h != null) {
				h.handle(target, baseRequest, request, response);
			}
		}
	}
	public void addHandler(ServerConnector sc, Handler rh) {
		handlerByConnector.put(sc, rh);
	}
}
