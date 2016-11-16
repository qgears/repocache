package hu.qgears.repocache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import hu.qgears.rtemplate.runtime.ICodeGeneratorContext;
import hu.qgears.rtemplate.runtime.TemplateTracker;

public class ClientQuery implements ICodeGeneratorContext
{
	private String target;
	private Request baseRequest;
	private HttpServletRequest request;
	public final HttpServletResponse response;
	public final RepoCache rc;
	public final Path path;
	
	public ClientQuery(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			RepoCache rc, Path path) {
		super();
		this.target = target;
		this.baseRequest = baseRequest;
		this.request = request;
		this.response = response;
		this.rc = rc;
		this.path=path;
	}

	@Override
	public boolean needReport() {
		return false;
	}

	@Override
	public void createFile(String path, String o) {
	}

	@Override
	public void createReport(String path, String o, TemplateTracker tt) {
	}

}
