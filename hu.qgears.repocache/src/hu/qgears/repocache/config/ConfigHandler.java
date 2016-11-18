package hu.qgears.repocache.config;

import java.io.IOException;

import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientSetup;
import hu.qgears.repocache.EClientMode;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.RepoHandler;

public class ConfigHandler {
	public void handle(ClientQuery q) throws IOException {
		QueryResponse ret=null;
		if(q.path.pieces.size()==1 && !q.path.folder)
		{
			RepoHandler.redirectToFolder(q);
		}
		if(q.path.eq(1, "config.xml"))
		{
			ret=new QueryResponse("application/xml", "", q.rc.getConfiguration().getConfigXml(), false);
		}
		if(q.path.pieces.size()==1)
		{
			ret=new ConfigListing(q).generate();
		}
		if(q.path.pieces.size()==2&&q.path.eq(1, "setClientMode"))
		{
			String client=q.getParameter("client");
			String mode=q.getParameter("mode");
			EClientMode emode=EClientMode.valueOf(mode);
			if(client.equals("this"))
			{
				client=q.getClientIdentifier();
			}
			ClientSetup cs=new ClientSetup(client, emode);
			q.rc.getConfiguration().setClientConfiguration(cs);
			q.sendRedirect("./");
			return;
		}
		if(q.path.pieces.size()==2&&q.path.eq(1, "commit"))
		{
			try {
				q.rc.getCommitTimer().executeCommit();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			q.sendRedirect("./");
			return;
		}
		if(ret!=null)
		{
			q.reply(ret.mimeType, ret.responseBody);
		}
	}
}
