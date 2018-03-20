package hu.qgears.repocache;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.util.StringUtils;

abstract public class ClientQuery
{
	private static Log log=LogFactory.getLog(ClientQuery.class);
	public final RepoCache rc;
	private final Path originalPath;
	private Path path;
	
	public ClientQuery(RepoCache rc, Path path) {
		super();
		this.rc = rc;
		this.originalPath=path;
		this.path=path;
	}

	abstract public String getClientIdentifier();

	abstract public String getParameter(String string);

	abstract public void sendRedirect(String string) throws IOException;
	abstract public void reply(QueryResponse responseBody) throws IOException;
	abstract public OutputStream createReplyStream(String mimeType) throws IOException;

	abstract public String[] getParameterValues(String string);

	abstract public Set<String> getParameterNames();

	public String getMimeType(QueryResponse resp) {
		if(resp!=null&&resp.contentType!=null)
		{
			return resp.contentType;
		}
		if(getPath().folder)
		{
			return "text/html;charset=utf-8";
		}
		String mimeType = "";
		try {
			mimeType = Files.probeContentType(new File(getPath().getFileName()).toPath());
		} catch (IOException e) {
			log.error("Error getting file mime type. path: " + getPath(), e);
		}
		return StringUtils.isEmptyOrNull(mimeType) ? "application/data" : mimeType;
	}

	/**
	 * The current path of the request (after resolving aliases).
	 * 
	 * @return
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * Returns the String representation of current path. Note that a '/' will be
	 * appended before the path string.
	 * 
	 * @return
	 */
	public String getPathString() {
		return "/"+getPath().toStringPath();
	}

	/**
	 * Returns the String representation of the original path. See {@link #getOriginalPath()}.
	 * Note that a '/' will be appended before the path string.
	 * 
	 * @return
	 */
	public String getOriginalPathString() {
		return "/"+getOriginalPath().toStringPath();
	}

	/**
	 * The original path of the request ( before resolving aliases)
	 * 
	 * @return
	 */
	public Path getOriginalPath() {
		return originalPath;
	}

	public void setPath(Path rewriteClientPath) {
		this.path=rewriteClientPath;
	}
}
