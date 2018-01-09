package hu.qgears.repocache.handler;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import hu.qgears.repocache.ClientQueryHttp;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.RepoCache;
import hu.qgears.repocache.config.ConfigHandler2;
import hu.qgears.repocache.folderlisting.CrawlExecutor;
import hu.qgears.repocache.log.AccessLogPage;

public class RepoHandler extends MyRequestHandler {
	private static Log log = LogFactory.getLog(RepoHandler.class);
	private ResourceHandler certsHandler;

	public RepoHandler(RepoCache rc) {
		super(rc);

		certsHandler = new ResourceHandler();
		certsHandler.setResourceBase("/certs/");
		certsHandler.setBaseResource(Resource.newResource(new File(rc.getArgs().getCertsFolder(), "public")));
		certsHandler.setDirectoriesListed(true);

	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		ClientQueryHttp q = new ClientQueryHttp(target, baseRequest, request, response, super.rc,
				new Path(baseRequest.getPathInfo()));
		String pathString=q.getPathString();
		if (!q.isPost()) {
			log.debug("Handling request arrived, path info: " + baseRequest.getPathInfo());
		}
		Path path = q.getPath();
		switch (path.toStringPath()) {
		case "config.html":
			new ConfigHandler2(q).handle();
			return;
		case "access-log.html":
			new AccessLogPage(q).handle();
			return;
		}
		if(pathString.startsWith("/certs/"))
		{
			baseRequest.setPathInfo(pathString.substring("/certs".length()));
			certsHandler.handle(target, baseRequest, request, response);
			return;
		}
		if (q.getParameter("crawl") != null) {
			new CrawlExecutor().handle(this, q);
		} else {
			super.handleQlientQuery(q, baseRequest, response, false);
		}
		if (!q.isPost()) {
			log.debug("Handling request response status: " + response.getStatus() + ", type: "
					+ response.getContentType());
		}
	}

}
