package hu.qgears.repocache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

abstract public class QueryResponse implements AutoCloseable
{
	public String url;
	public boolean folder;
	public File fileSystemFolder;

	public QueryResponse(String url) throws IOException {
		this.url=url;
		folder=url.endsWith("/");
	}
	public QueryResponse(String url, boolean folder){
		this.url=url;
		this.folder=folder;
	}
	@Override
	public String toString() {
		return "Response: URL: "+url;
	}
	/**
	 * URL is ignored, body must match.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof QueryResponse)
		{
			QueryResponse other=(QueryResponse) obj;
			if(folder==other.folder)
			{
				try {
					return contentEq(other);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false;
		}
		return super.equals(obj);
	}
	private boolean contentEq(QueryResponse other) throws FileNotFoundException, IOException {
		try(InputStream is0=openInputStream())
		{
			try(InputStream is1=other.openInputStream())
			{
				return IOUtils.contentEquals(is0, is1);
			}
		}
	}
	abstract public InputStream openInputStream() throws FileNotFoundException;
	abstract public void updateContent(byte[] bytes) throws IOException;
	abstract public String getResponseAsString() throws FileNotFoundException, IOException;
	abstract public byte[] getResponseAsBytes() throws FileNotFoundException, IOException;
	/**
	 * Move response content to the file.
	 * If response is in a temporary file then move the file.
	 * If response is in a byte buffer then save it.
	 * @param workingCopyFile
	 * @throws IOException 
	 */
	abstract public void saveToFile(File workingCopyFile) throws IOException;
	abstract public void streamTo(OutputStream outputStream) throws FileNotFoundException, IOException;
	@Override
	abstract public void close();
}
