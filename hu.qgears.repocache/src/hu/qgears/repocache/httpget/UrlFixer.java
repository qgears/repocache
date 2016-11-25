package hu.qgears.repocache.httpget;

import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.repocache.QueryResponse;

/**
 * Parse result HTML and convert absolute lints to relative so that they work on the mirror site.
 */
public class UrlFixer {
	private static Logger log=LoggerFactory.getLogger(UrlFixer.class);

	public static void fixUrls(QueryResponse queryResponse) {
		// Fix absolute URL-s
		try
		{
			String fixedurl=queryResponse.url;
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
			String html=queryResponse.getResponseAsString();
			Document doc=Jsoup.parse(html);
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
			queryResponse.updateContent(doc.toString().getBytes(StandardCharsets.UTF_8));
		}catch(Exception e)
		{
			log.error("Error fixing URL: " + queryResponse.url, e);
		}
	}
	private static String getWithoutServer(String fixedurl) {
		int idx=fixedurl.indexOf("://");
		if(idx>=0)
		{
			int newidx=fixedurl.indexOf("/", idx+3);
			if(newidx>=0)
			{
				return fixedurl.substring(newidx);
			}
		}
		return null;
	}

}
