package hu.qgears.repocache.httpget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;

import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.QueryResponseFile;

public class HttpGet {
	public final String url;
	private boolean ready;
	private File tmpFile;
	public HttpGet(File tmpFile, String url) {
		super();
		this.url = url;
		this.tmpFile=tmpFile;
	}

	/**
	 * Create output stream for result.
	 * @param l length of reply: -1 means unknown.
	 * @return
	 * @throws FileNotFoundException 
	 */
	public OutputStream createOutputStream(long l) throws FileNotFoundException {
		return new FileOutputStream(tmpFile);
	}

	public void close() {
		if(!ready)
		{
			tmpFile.delete();
		}
	}

	public QueryResponse ready(GetMethod method) throws URIException, IOException {
		this.ready=true;
		return new QueryResponseFile(method.getURI().toString(), tmpFile).setDeleteFileOnClose(true);
	}
	public File getFile()
	{
		return tmpFile;
	}
}
