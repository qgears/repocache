package hu.qgears.repocache;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import hu.qgears.rtemplate.runtime.ICodeGeneratorContext;
import hu.qgears.rtemplate.runtime.TemplateTracker;

abstract public class ClientQuery implements ICodeGeneratorContext
{
	public final RepoCache rc;
	public final Path path;
	
	public ClientQuery(RepoCache rc, Path path) {
		super();
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

	abstract public String getClientIdentifier();

	abstract public String getParameter(String string);

	abstract public void sendRedirect(String string) throws IOException;
	abstract public void reply(String mimeType, byte[] responseBody) throws IOException;
	abstract public OutputStream createReplyStream(String mimeType) throws IOException;

	abstract public String[] getParameterValues(String string);

	abstract public Set<String> getParameterNames();
}
