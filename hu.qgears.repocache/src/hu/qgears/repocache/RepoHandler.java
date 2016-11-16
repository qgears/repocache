package hu.qgears.repocache;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepoHandler extends AbstractHandler {
	private RepoCache rc;
	private Logger log=LoggerFactory.getLogger(RepoHandler.class);
	public RepoHandler(RepoCache rc) {
		this.rc = rc;
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
//		System.out.println("client connection: "+request.getRemoteHost());
		ClientQuery q=new ClientQuery(target, baseRequest, request, response, rc, new Path(baseRequest.getPathInfo()));
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
		if(cachedContent!=null)
		{
			if(!q.path.folder && cachedContent.folder)
			{
				redirectToFolder(q);
				return;
//				response.sendRedirect(q.path.pieces.get(q.path.pieces.size()-1)+"/");
			}else
			{
				response.setContentType(cachedContent.mimeType);
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getOutputStream().write(cachedContent.responseBody);
			}
		}
//		if(q.path.pieces.size()>0&&q.path.pieces.get(0).equals(p2))
//		{
//			Path path=new Path(q.path).remove(0);
//			if(path.pieces.size()==0)
//			{
//				if(!q.path.folder)
//				{
//					redirectToFolder(q);
//					return;
//				}else
//				{
//					new P2Listing(q).generate();
//				}
//			}else if(path.pieces.size()>0&&path.pieces.get(0).equals(p2repopath))
//			{
//				try {
//					Path p=new Path(path).remove(0);
//					QueryResponse resp=rc.getContent(p);
//					if(!q.path.folder && resp.folder)
//					{
//						redirectToFolder(q);
//						return;
////						response.sendRedirect(q.path.pieces.get(q.path.pieces.size()-1)+"/");
//					}else
//					{
//						response.setContentType(resp.mimeType);
//						response.setStatus(HttpServletResponse.SC_OK);
//						baseRequest.setHandled(true);
//						response.getOutputStream().write(resp.responseBody);
//					}
//				} catch (Exception e) {
//					response.setContentType("text/html;charset=utf-8");
//					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
//					baseRequest.setHandled(true);
//					response.getWriter().write("Error");
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}else if(!path.folder&&path.pieces.size()==1 &&path.pieces.get(0).equals(P2CompositeArtifacts.file))
//			{
//				new P2CompositeArtifacts(q).generate();
//			}else if(!path.folder&&path.pieces.size()==1 &&path.pieces.get(0).equals(P2CompositeContent.file))
//			{
//				new P2CompositeContent(q).generate();
//			}
//		}
//		else
//		{
//			if(q.path.pieces.size()==0)
//			{
//				new StatusPage(q).generate();
//			}
//		}
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

	private void redirectToFolder(ClientQuery q) throws IOException {
		q.response.sendRedirect(q.path.pieces.get(q.path.pieces.size()-1)+"/");
	}
}
