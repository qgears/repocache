package hu.qgears.repocache.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.repocache.AbstractRepoPlugin;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.RepoCache;
import hu.qgears.repocache.StatusPage;
import hu.qgears.repocache.config.ClientSetup;
import hu.qgears.repocache.folderlisting.RealFolderListing;

public abstract class MyRequestHandler extends AbstractHandler {
	private static Log log=LogFactory.getLog(RepoHandler.class);
	
	protected RepoCache rc;
	public MyRequestHandler(RepoCache rc) {
		this.rc = rc;
	}

	protected void handleQlientQuery (ClientQuery q, Request baseRequest, HttpServletResponse response, boolean rw) throws IOException, ServletException {
		try(QueryResponse cachedContent=getQueryResponse(q, rw)) {
			if(cachedContent!=null) {
				if(!q.path.folder && cachedContent.folder) {
					redirectToFolder(q);
				} else {
					response.setContentType(q.getMimeType(cachedContent));
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentLength(cachedContent.getLength());
					baseRequest.setHandled(true);
					cachedContent.streamTo(response.getOutputStream());
					if(cachedContent.fileSystemFolder!=null) {
						appendRealFolderListing(q, response, cachedContent);
					}
				}
			} else {
				if (q.path.folder) {
					QueryResponse qr = rc.loadDirFromCache(q.path);
					if (qr != null) {
						response.setContentType(q.getMimeType(cachedContent));
						response.setStatus(HttpServletResponse.SC_OK);
						baseRequest.setHandled(true);
						QueryResponse r2=new RealFolderListing(q, qr).generate();
						r2.streamTo(response.getOutputStream());
						response.setContentLength(r2.getLength());
					} else {
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					}
				} else {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}
			}
		}
	}
	
	public QueryResponse getQueryResponse(ClientQuery q, boolean rw) throws IOException {
		if(rc.getAccessRules().isRepoTransparent(q))
		{
			log.trace("Getting response from transparent repo : " + q.path.toStringPath());
			QueryResponse qr=getResponseFromPlugin(q, null, true);
			return qr;
		}
		QueryResponse cachedContent=rc.getCache(q.path);
		boolean updateRequired=q.rc.updateRequired(q, cachedContent, rw);
		QueryResponse qr=getResponseFromPlugin(q, cachedContent, updateRequired);
		log.debug("HANDLE: '" + q.path.toStringPath() +"' "+ (updateRequired?"UPDATED":(cachedContent==null?"NO CACHE":("CACHE: "+cachedContent))));
		if(qr!=null)
		{
			try
			{
				try {
					rc.updateResponse(q.path, cachedContent, qr);
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

	private void appendRealFolderListing(ClientQuery q, HttpServletResponse response, QueryResponse cachedContent) throws IOException {
		ClientSetup client=rc.getConfiguration().getClientSetup(q.getClientIdentifier());
		if (client.isShawRealFolderListing()) {
			QueryResponse r2=new RealFolderListing(q, cachedContent).generate();
			response.getOutputStream().write(r2.getResponseAsBytes());
		}
	}

	private QueryResponse getResponseFromPlugin(ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		Path path=rc.getConfiguration().doPathAlias(q.path);
		try {
			for(AbstractRepoPlugin plugin: rc.getPlugins())
			{
				if(path.eq(0, plugin.getPath()))
				{
					return plugin.getOnlineResponse(path, new Path(path).remove(0), q, cachedContent, netAllowed);
				}
			}
			if(path.pieces.size()==0)
			{
				return new StatusPage(q).generate();
			}
		} catch (Exception e) {
			log.debug("Error fetching file: "+path + ", message: " + e.getMessage());
		}
		return cachedContent;
	}

	public static void redirectToFolder(ClientQuery q) throws IOException {
		q.sendRedirect(q.path.pieces.get(q.path.pieces.size()-1)+"/");
	}

}
