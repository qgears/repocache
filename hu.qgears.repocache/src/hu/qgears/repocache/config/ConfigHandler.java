package hu.qgears.repocache.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientSetup;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.QueryResponseByteArray;
import hu.qgears.repocache.RepoHandler;

public class ConfigHandler {
	private static Logger log=LoggerFactory.getLogger(ConfigHandler.class);
	
	public void handle(ClientQuery q) throws IOException {
		QueryResponse ret=null;
		if(q.path.pieces.size()==1 && !q.path.folder)
		{
			RepoHandler.redirectToFolder(q);
		}
		if(q.path.eq(1, "config.xml"))
		{
			ret=new QueryResponseByteArray("config.xml", q.rc.getConfiguration().getConfigXml());
		}
		if(q.path.eq(1, "repoModeConfig"))
		{
			if (q.path.pieces.size()==3&&q.path.eq(2, "setRepoMode")) {
				String repoName=q.getParameter("repoName");
				String mode=q.getParameter("mode");
				log.info("Setting repoMode on repo name: " + repoName + ", to mode: " + mode);
				q.rc.getRepoModeHandler().setRepoMode(repoName, RepoMode.parse(mode));
				q.sendRedirect("./");
				return;
			}
			ret=new RepoModeListing(q).generate();
		}
		if(q.path.pieces.size()==1)
		{
			ret=new ConfigListing(q).generate();
		}
		if(q.path.pieces.size()==2&&q.path.eq(1, "setClientMode"))
		{
			String client=q.getParameter("client");
			String mode=q.getParameter("mode");
			EClientMode emode=(mode==null?null:EClientMode.valueOf(mode));
			String validInMinute=q.getParameter("validInMinute");
			String shawRealFolderListing=q.getParameter("shawRealFolderListing");
			log.info("Setting client mode, client: " + client + ", mode: " + mode + ", validinmin");
			if(client.equals("this"))
			{
				client=q.getClientIdentifier();
			}
			ClientSetup cs=q.rc.getConfiguration().getClientSetup(client);
			if (emode!=null) {
				cs.setMode(emode, validInMinute);
			}
			if (shawRealFolderListing!=null) {
				cs.setShawRealFolderListing(Boolean.valueOf(shawRealFolderListing));
			}
			q.sendRedirect("./");
			return;
		}
		if(q.path.pieces.size()==2&&q.path.eq(1, "commit"))
		{
			try {
				q.rc.getCommitTimer().executeCommit();
			} catch (Exception e) {
				e.printStackTrace();
			}
			q.sendRedirect("./");
			return;
		}
		if(q.path.pieces.size()==2&&q.path.eq(1, "revert"))
		{
			try {
				q.rc.getCommitTimer().executeRevert();
			} catch (Exception e) {
				e.printStackTrace();
			}
			q.sendRedirect("./");
			return;
		}
		if(ret!=null)
		{
			q.reply(ret);
		}
	}
	
}
