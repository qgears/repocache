package hu.qgears.repocache;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.repocache.config.ClientSetup;
import hu.qgears.repocache.config.ConfigHandler;
import hu.qgears.repocache.folderlisting.CrawlExecutor;
import hu.qgears.repocache.folderlisting.RealFolderListing;

public class RepoHandler extends AbstractHandler {
	private static Log log=LogFactory.getLog(RepoHandler.class);
	
	private RepoCache rc;
	public RepoHandler(RepoCache rc) {
		this.rc = rc;
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		log.info("Handling request, path info: " + baseRequest.getPathInfo());
		ClientQuery q=new ClientQueryHttp(target, baseRequest, request, response, rc, new Path(baseRequest.getPathInfo()));
		if(q.path.eq(0, "config")) {
			new ConfigHandler().handle(q);
		} else if(q.getParameter("crawl")!=null) {
			new CrawlExecutor().handle(this, q);
		} else {
			try(QueryResponse cachedContent=getQueryResponse(q)) {
				if(cachedContent!=null) {
					if(!q.path.folder && cachedContent.folder) {
						redirectToFolder(q);
					} else {
						response.setContentType(q.getMimeType());
						response.setStatus(HttpServletResponse.SC_OK);
						baseRequest.setHandled(true);
						cachedContent.streamTo(response.getOutputStream());
						if(cachedContent.fileSystemFolder!=null) {
							appendRealFolderListing(q, response, cachedContent);
						}
					}
				} else {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}
			}
		}
		log.info("Handling request response status: " + response.getStatus() + ", type: " + response.getContentType());
	}

	public QueryResponse getQueryResponse(ClientQuery q) throws IOException {
		if(q.path.pieces.size()>1 && rc.getRepoModeHandler().isRepoTransparent(q.path.pieces.get(1)))
		{
			log.trace("Getting response from transparent repo : " + q.path.pieces.get(1));
			QueryResponse qr=getResponseFromPlugin(q, null, true);
			return qr;
		}
		QueryResponse cachedContent=rc.getCache(q.path);
		QueryResponse qr=getResponseFromPlugin(q, cachedContent, q.rc.updateRequired(q, cachedContent));
		if(qr!=null)
		{
			try
			{
				try {
					updateResponseByPlugin(q, cachedContent, qr);
				} catch (Exception e) {
					throw new IOException(e);
				}
				cachedContent=rc.getCache(q.path);
			}finally
			{
				qr.close();
			}
		}
		return cachedContent;
	}

	private void updateResponseByPlugin(ClientQuery q, QueryResponse cachedContent, QueryResponse qr) throws Exception {
		AbstractRepoPlugin plugin = null;
		for(AbstractRepoPlugin pl: rc.getPlugins())
		{
			if(q.path.eq(0, pl.getPath()))
			{
				plugin = pl;
				break;
			}
		}
		rc.updateResponse(q.path, cachedContent, qr, plugin);
	}
	
	private void appendRealFolderListing(ClientQuery q, HttpServletResponse response, QueryResponse cachedContent) throws IOException {
		ClientSetup client=rc.getConfiguration().getClientSetup(q.getClientIdentifier());
		if (client.isShawRealFolderListing()) {
			QueryResponse r2=new RealFolderListing(q, cachedContent).generate();
			response.getOutputStream().write(r2.getResponseAsBytes());
		}
	}

	private QueryResponse getResponseFromPlugin(ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		try {
			for(AbstractRepoPlugin plugin: rc.getPlugins())
			{
				if(q.path.eq(0, plugin.getPath()))
				{
					return plugin.getOnlineResponse(new Path(q.path).remove(0), q, cachedContent, netAllowed);
				}
			}
			if(q.path.pieces.size()==0)
			{
				return new StatusPage(q).generate();
			}
		} catch (Exception e) {
			log.info("Error fetching file: "+q.path + ", message: " + e.getMessage());
		}
		return cachedContent;
	}

	public static void redirectToFolder(ClientQuery q) throws IOException {
		q.sendRedirect(q.path.pieces.get(q.path.pieces.size()-1)+"/");
	}
}
