package hu.qgears.repocache.folderlisting;

import java.io.File;

import hu.qgears.commons.UtilFile;
import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.RepoCache;

public class RealFolderListing extends AbstractPage
{
	QueryResponse resp;

	public RealFolderListing(ClientQuery query, QueryResponse resp) {
		super(query);
		this.resp=resp;
	}

	@Override
	protected void doGenerate() {
		write("<hr/>\nReal folder listing: <a href=\"./?crawl=true\">Crawl all files below by folder listings.</a><br/>\n<a href=");
		writeValue("../");
		write(">");
		writeHtml("../");
		write("</a><br/>\n");
		for(File f: UtilFile.listFiles(resp.fileSystemFolder))
		{
			if(f.getName().startsWith(RepoCache.maintenancefilesprefix))
			{
				continue;
			}
			if(f.getName().equals(".git"))
			{
				continue;
			}
			String name=f.getName()+(f.isDirectory()?"/":"");
			write("<a href=");
			writeValue(name);
			write(">");
			writeHtml(name);
			write("</a><br/>\n");
		}
	}
}
