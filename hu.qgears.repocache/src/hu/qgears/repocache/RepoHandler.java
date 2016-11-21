package hu.qgears.repocache;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.repocache.config.ConfigHandler;
import hu.qgears.repocache.folderlisting.CrawlExecutor;
import hu.qgears.repocache.folderlisting.RealFolderListing;

public class RepoHandler extends AbstractHandler {
	private RepoCache rc;
	private Logger log=LoggerFactory.getLogger(RepoHandler.class);
	public RepoHandler(RepoCache rc) {
		this.rc = rc;
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		ClientQuery q=new ClientQueryHttp(target, baseRequest, request, response, rc, new Path(baseRequest.getPathInfo()));
		if(q.path.eq(0, "config"))
		{
			new ConfigHandler().handle(q);
			return;
		}
		if(q.getParameter("crawl")!=null)
		{
			new CrawlExecutor().handle(this, q);
			return;
		}
		QueryResponse cachedContent=getQueryResponse(q);
		if(cachedContent!=null)
		{
			if(!q.path.folder && cachedContent.folder)
			{
				redirectToFolder(q);
				return;
//				response.sendRedirect(q.path.pieces.get(q.path.pieces.size()-1)+"/");
			}else
			{
				response.setContentType(q.getMimeType());
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getOutputStream().write(cachedContent.responseBody);
				if(cachedContent.fileSystemFolder!=null)
				{
					appendRealFolderListing(q, response, cachedContent);
				}
			}
		}
	}

	public QueryResponse getQueryResponse(ClientQuery q) throws IOException {
		if(rc.getConfiguration().getClientSetup(q.getClientIdentifier()).isNoCacheTransparent())
		{
			QueryResponse qr=getResponseFromPlugin(q, null, true);
			return qr;
		}
		QueryResponse cachedContent=rc.getCache(q.path);
		QueryResponse qr=getResponseFromPlugin(q, cachedContent, q.rc.updateRequired(q, cachedContent));
		if(qr!=null)
		{
			try {
				rc.updateResponse(q.path, cachedContent, qr);
			} catch (Exception e) {
				throw new IOException(e);
			}
			cachedContent=rc.getCache(q.path);
		}
		return cachedContent;
	}

	private void appendRealFolderListing(ClientQuery q, HttpServletResponse response, QueryResponse cachedContent) throws IOException {
		ClientSetup client=rc.getConfiguration().getClientSetup(q.getClientIdentifier());
		if (client.isShawRealFolderListing()) {
			QueryResponse r2=new RealFolderListing(q, cachedContent).generate();
			response.getOutputStream().write(r2.responseBody);
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
			System.err.println("Error fetching file: "+q.path);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cachedContent;
	}

	public static void redirectToFolder(ClientQuery q) throws IOException {
		q.sendRedirect(q.path.pieces.get(q.path.pieces.size()-1)+"/");
	}
}
