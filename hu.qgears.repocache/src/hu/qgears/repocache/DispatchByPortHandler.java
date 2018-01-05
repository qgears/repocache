package hu.qgears.repocache;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class DispatchByPortHandler extends AbstractHandler
{
	private Map<Integer, Handler> handlers=new TreeMap<>();
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		Handler h=handlers.get(baseRequest.getServerPort());
		if(h!=null)
		{
			h.handle(target, baseRequest, request, response);
		}
	}

}
