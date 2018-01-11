package hu.qgears.repocache;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import hu.qgears.quickjs.utils.InMemoryPost;

public class ClientQueryHttp extends ClientQuery
{
	public Request baseRequest;
	public final HttpServletResponse response;
	private InMemoryPost inMemoryPost;

	public ClientQueryHttp(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			RepoCache rc, Path path) {
		super(rc, path);
		this.baseRequest = baseRequest;
		this.response = response;
	}
	@Override
	public String getClientIdentifier() {
		return baseRequest.getRemoteAddr();
	}
	@Override
	public String getParameter(String string) {
		return baseRequest.getParameter(string);
	}
	@Override
	public void sendRedirect(String string) throws IOException {
		response.sendRedirect(string);
	}
	@Override
	public void reply(QueryResponse r) throws IOException
	{
		response.setContentType(getMimeType(r));
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		r.streamTo(response.getOutputStream());
	}
	@Override
	public String[] getParameterValues(String name) {
		String[] ret=baseRequest.getParameterValues(name);
		if(ret==null)
		{
			ret=new String[]{};
		}
		return ret;
	}
	@Override
	public Set<String> getParameterNames() {
		Set<String> ret=new HashSet<>();
		Enumeration<String> e=baseRequest.getParameterNames();
		while(e.hasMoreElements())
		{
			ret.add(e.nextElement());
		}
		return ret;
	}
	@Override
	public OutputStream createReplyStream(String mimeType) throws IOException {
		response.setContentType(mimeType);
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		return response.getOutputStream();
	}
	public boolean isPost() {
		return "POST".equals(baseRequest.getMethod());
	}
	public InMemoryPost getInMemoryPost() throws IOException
	{
		if(inMemoryPost==null)
		{
			try {
				inMemoryPost=new InMemoryPost(baseRequest);
			} catch (Exception e) {
				throw new IOException("Parsing multipart data", e);
			}			
		}
		return inMemoryPost;
	}
}
