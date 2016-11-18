package hu.qgears.repocache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import hu.qgears.repocache.httpget.HttpGet;

public class QueryResponse {
	public String mimeType="text/html";
	public String url;
	public byte[] responseBody;
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
		responseBody=method.getResponseBody();
		folder=url.endsWith("/");
		if(folder)
		{
			// Fix absolute URL-s
			try
			{
				String fixedurl=url;
				// remove trailing /'s (For some reason in some cases there are multiple trailing /'s)
				while(fixedurl.endsWith("/"))
				{
					fixedurl=fixedurl.substring(0, fixedurl.length()-1);
				}
				int lastSlash=fixedurl.lastIndexOf("/");
				String parent=fixedurl.substring(0,lastSlash+1);
				fixedurl=fixedurl+"/";
				String withoutServer=getWithoutServer(fixedurl);
				String withoutServerParent=getWithoutServer(parent);
				Document doc=Jsoup.parse(new String(responseBody, StandardCharsets.UTF_8));
				Elements elems=doc.select("a");
				for(Element e: elems)
				{
					String href=e.attr("href");
					String preHref=href;
					if(href!=null)
					{
						if(href.startsWith(fixedurl))
						{
							href=href.substring(fixedurl.length());
						}else if(href.startsWith(parent))
						{
							href="../"+href.substring(parent.length());
						}else if(withoutServer!=null&&href.startsWith(withoutServer))
						{
							href=href.substring(withoutServer.length());
						}else if(withoutServerParent!=null&&href.startsWith(withoutServerParent))
						{
							href="../"+href.substring(withoutServerParent.length());
						}
						if(!href.equals(preHref))
						{
							e.attr("href", href);
						}
					}
				}
				responseBody=doc.toString().getBytes(StandardCharsets.UTF_8);
			}catch(Exception e)
			{
				// TODO
				e.printStackTrace();
			}
		}
	}
	private String getWithoutServer(String fixedurl) {
		int idx=fixedurl.indexOf("://");
		if(idx>=0)
		{
			int newidx=fixedurl.indexOf("/", idx+3);
			if(newidx>=0)
			{
				return fixedurl.substring(newidx);
			}
		}
		// TODO Auto-generated method stub
		return null;
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
