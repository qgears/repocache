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
import hu.qgears.repocache.config.ConfigHandler;
import hu.qgears.repocache.folderlisting.CrawlExecutor;

public class RepoHandler extends MyRequestHandler {
	private static Log log=LogFactory.getLog(RepoHandler.class);
	
	public RepoHandler(RepoCache rc) {
		super(rc);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		log.debug("Handling request arrived, path info: " + baseRequest.getPathInfo());
		ClientQuery q=new ClientQueryHttp(target, baseRequest, request, response, super.rc, new Path(baseRequest.getPathInfo()));
		if(q.path.eq(0, "config")) {
			new ConfigHandler().handle(q);
		} else if(q.getParameter("crawl")!=null) {
			new CrawlExecutor().handle(this, q);
		} else {
			super.handleQlientQuery(q, baseRequest, response, false);
		}
		log.debug("Handling request response status: " + response.getStatus() + ", type: " + response.getContentType());
	}

}
