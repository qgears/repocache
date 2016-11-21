package hu.qgears.repocache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public class ClientQueryInternal extends ClientQuery
{
	private ClientQuery delegate;
	public ClientQueryInternal(RepoCache rc, Path path, ClientQuery delegate) {
		super(rc, path);
		this.delegate=delegate;
	}

	@Override
	public String getClientIdentifier() {
		return delegate.getClientIdentifier();
	}

	@Override
	public String getParameter(String string) {
		return delegate.getParameter(string);
	}

	@Override
	public void sendRedirect(String string) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void reply(byte[] responseBody) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getParameterValues(String string) {
		return delegate.getParameterValues(string);
	}

	@Override
	public Set<String> getParameterNames() {
		return delegate.getParameterNames();
	}

	@Override
	public OutputStream createReplyStream(String mimeType) throws IOException {
		return new ByteArrayOutputStream();
	}

}
