package hu.qgears.repocache.httpget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

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

	public void ready() {
		this.ready=true;
	}
	public File getFile()
	{
		return tmpFile;
	}
}
