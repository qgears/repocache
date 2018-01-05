package hu.qgears.repocache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class DispatchByPortHandler extends AbstractHandler
{
	private Map<ServerConnector, Handler> handlerByConnector=new HashMap<>();
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		Connector c=baseRequest.getHttpChannel().getConnector();
		Handler h=handlerByConnector.get(c);
		if(h!=null)
		{
			h.handle(target, baseRequest, request, response);
		}
	}
	public void addHandler(ServerConnector sc, Handler rh) {
		handlerByConnector.put(sc, rh);
	}
}
