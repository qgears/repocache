package hu.qgears.repocache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.httpclient.methods.GetMethod;

import hu.qgears.commons.UtilFile;
import hu.qgears.repocache.httpget.HttpGet;
import hu.qgears.repocache.httpget.UrlFixer;

public class QueryResponse {
	public String mimeType="text/html";
	public String url;
	private byte[] responseBody;
	private File file;
	public boolean folder;
	public File fileSystemFolder;

	public QueryResponse(GetMethod method, HttpGet get) throws IOException {
		try {
			mimeType=method.getResponseHeader("Content-Type").getValue().split(";")[0].trim();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		url=method.getURI().toString();
		if(get.getFile()!=null)
		{
			file=get.getFile();
		}else
		{
			responseBody=method.getResponseBody();
		}
		folder=url.endsWith("/");
		if(folder)
		{
			UrlFixer.fixUrls(this);
		}
	}
	@Override
	public String toString() {
		return "Response: URL: "+url+" mime type: "+mimeType;
	}
	public QueryResponse(String mimeType, String url, byte[] responseBody, boolean folder) {
		super();
		this.mimeType = mimeType;
		this.url = url;
		this.responseBody = responseBody;
		this.folder=folder;
	}
	public static QueryResponse createFromContentAndMeta(byte[] listing, byte[] listingMeta, boolean folder) {
		return new QueryResponse(new String(listingMeta, StandardCharsets.UTF_8), "", listing, folder);
	}
	public byte[] createMeta() {
		return mimeType.getBytes(StandardCharsets.UTF_8);
	}
	/**
	 * URL is ignored, mimetype and body must match.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof QueryResponse)
		{
			QueryResponse other=(QueryResponse) obj;
			return Arrays.equals(responseBody, other.responseBody)&& mimeType.equals(other.mimeType)
					&&(folder==other.folder);
		}
		return super.equals(obj);
	}
	public InputStream openInputStream() throws FileNotFoundException {
		return new FileInputStream(file);
	}
	public void updateContent(byte[] bytes) throws IOException {
		UtilFile.saveAsFile(file, bytes);
	}
	public String getResponseAsString() throws FileNotFoundException, IOException {
		return UtilFile.loadAsString(openInputStream());
	}
	public byte[] getResponseAsBytes() throws FileNotFoundException, IOException {
		return UtilFile.loadFile(openInputStream());
	}
	
}
