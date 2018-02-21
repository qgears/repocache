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
import hu.qgears.repocache.config.RepoConfiguration;

public class HttpGet {
	public final String url;
	private final File tmpFile;
	private final RepoConfiguration repoConfiguration;
	private boolean ready;
	
	/**
	 * Creates an instance of HTTP GET wrapper. 
	 * @param tmpFile a temporary file, into which data will be downloaded
	 * @param url the URL from which to download
	 * @param repoConfiguration the configuration, from which HTTP connection 
	 * and socket timeouts will be read
	 */
	public HttpGet(File tmpFile, String url, RepoConfiguration repoConfiguration) {
		super();
		this.url = url;
		this.tmpFile=tmpFile;
		this.repoConfiguration = repoConfiguration;
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
		QueryResponseFile ret=new QueryResponseFile(method.getURI().toString(), 
				tmpFile).setDeleteFileOnClose(true);
		return ret;
	}
	
	public File getFile() {
		return tmpFile;
	}
	
	public RepoConfiguration getRepoConfiguration() {
		return repoConfiguration;
	}
}
