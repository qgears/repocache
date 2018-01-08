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
import hu.qgears.repocache.config.ClientSetup;
import hu.qgears.repocache.folderlisting.RealFolderListing;

public abstract class MyRequestHandler extends AbstractHandler {
	private static Log log=LogFactory.getLog(RepoHandler.class);
	
	protected RepoCache rc;
	public MyRequestHandler(RepoCache rc) {
		this.rc = rc;
	}

	protected void handleQlientQuery (ClientQuery q, Request baseRequest, HttpServletResponse response, boolean rw) throws IOException, ServletException {
		q.setPath(q.rc.getAccessRules().rewriteClientPath(q.getPath()));
		try(QueryResponse cachedContent=getQueryResponse(q, rw)) {
			if(cachedContent!=null) {
				if(!q.getPath().folder && cachedContent.folder) {
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
				if (q.getPath().folder) {
					QueryResponse qr = rc.loadDirFromCache(q.getPath());
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
			log.trace("Getting response from transparent repo : " + q.getPathString());
			QueryResponse qr=getResponseFromPlugin(q, null, true);
			return qr;
		}
		QueryResponse cachedContent=rc.getCache(q.getPath());
		boolean updateRequired=q.rc.updateRequired(q, cachedContent, rw);
		QueryResponse qr=getResponseFromPlugin(q, cachedContent, updateRequired);
		log.debug("HANDLE: '" + q.getPathString() +"' "+ (updateRequired?"UPDATED":(cachedContent==null?"NO CACHE":("CACHE: "+cachedContent))));
		if(qr!=null)
		{
			try
			{
				try {
					rc.updateResponse(q.getPath(), cachedContent, qr);
				} catch (Exception e) {
					throw new IOException(e);
				}
				cachedContent=rc.getCache(q.getPath());
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
		Path path=rc.getAccessRules().rewriteInternetPath(q.getPath());
		try {
			AbstractRepoPlugin plugin=rc.getPlugin(path);
			if(plugin!=null)
			{
				return plugin.getOnlineResponse(path, new Path(path).remove(0), q, cachedContent, netAllowed);
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
		q.sendRedirect(q.getPath().pieces.get(q.getPath().pieces.size()-1)+"/");
	}

}
