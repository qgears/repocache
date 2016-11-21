package hu.qgears.repocache.folderlisting;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientQueryInternal;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.RepoHandler;

public class CrawlExecutor {

	public void handle(RepoHandler rh, ClientQuery q) throws IOException {
		try(OutputStream os=q.createReplyStream("text/html"))
		{
			try(Writer w=new OutputStreamWriter(os, StandardCharsets.UTF_8))
			{
				w.write("<pre>");
				handleInternal(rh, q, w);
				w.write("OK.");
				w.write("</pre>");
			}
		}
	}
	private void handleInternal(RepoHandler rh, ClientQuery q, Writer w) throws IOException
	{
		w.write("Querying: "+q.path.toStringPath()+"\n");
		w.flush();
		QueryResponse resp=rh.getQueryResponse(q);
		if(resp!=null&&resp.folder)
		{
			try {
				Document doc=Jsoup.parse(resp.getResponseAsString());
				Elements elems=doc.select("a");
				for(Element e: elems)
				{
					String href=e.attr("href");
					int qm=href.indexOf('?');
					if(qm>=0)
					{
						href=href.substring(0, qm);
					}
					if(!href.startsWith(".")&&!isUrl(href))
					{
						try
						{
							Path relpath=new Path(q.path, href);
							if(relpath.pieces.size()>q.path.pieces.size())
							{
								relpath.validate();
								w.write("Delegate child: "+q.path.toStringPath()+": "+relpath.toStringPath()+"\n");
								w.flush();
								ClientQueryInternal subq=new ClientQueryInternal(q.rc, relpath, q);
								handleInternal(rh, subq, w);
							}
						}catch(Exception ex)
						{
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean isUrl(String href) {
		return href.indexOf("://")>=0;
	}

}
