package hu.qgears.repocache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.httpclient.methods.GetMethod;

public class QueryResponse {
	public String mimeType="text/html";
	public String url;
	public byte[] responseBody;
	public boolean folder;

	public QueryResponse(GetMethod method) throws IOException {
		try {
			mimeType=method.getResponseHeader("Content-Type").getValue().split(";")[0].trim();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		url=method.getURI().toString();
		responseBody=method.getResponseBody();
		folder=url.endsWith("/");
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
	
}
