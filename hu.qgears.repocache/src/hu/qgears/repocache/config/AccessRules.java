package hu.qgears.repocache.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.commons.UtilString;
import hu.qgears.repocache.ClientQuery;

public class AccessRules {
	private static Log log=LogFactory.getLog(AccessRules.class);
	class AccessRule
	{
		public RepoMode mode;
		public String path;
		public AccessRule(RepoMode mode, String path) {
			super();
			this.mode = mode;
			this.path = path;
		}
	}
	private String config="";
	private Object syncObject=new Object();
	private List<AccessRule> rules=new ArrayList<>();
	public String getConfig() {
		synchronized (syncObject) {
			return config;
		}
	}
	public void setConfig(String config)
	{
		synchronized (syncObject) {
			log.info("Access rules reloaded");
			this.config=config;
			rules.clear();
			List<String> lines=UtilString.split(config, "\r\n");
			for(String line:lines)
			{
				List<String> pieces=UtilString.split(line, "\t ");
				if(pieces.size()==2)
				{
					String mode=pieces.get(0);
					RepoMode rm=parseMode(mode);
					if(rm!=null)
					{
						String path=pieces.get(1);
						if(!path.startsWith("/"))
						{
							path="/"+path;
						}
						rules.add(new AccessRule(rm, path));
						log.info("Access rule: "+rm+" '"+path+"'");
					}
				}else
				{
					// Log omit
				}
			}
		}
	}
	private RepoMode parseMode(String mode) {
		switch (mode) {
		case "ro":
			return RepoMode.READ_ONLY;
		case "add":
			return RepoMode.ADD_ONLY;
		case "update":
			return RepoMode.UPDATE;
		case "transparent":
			return RepoMode.NO_CACHE_TRANSPARENT;
		default:
			return null;
		}
	}
	public boolean isRepoTransparent(ClientQuery q) {
		RepoMode mode=getMode(q);
		return RepoMode.NO_CACHE_TRANSPARENT==mode;
	}
	private RepoMode getMode(ClientQuery q) {
		String path="/"+q.path.toStringPath();
		synchronized (syncObject) {
			for(AccessRule rule:rules)
			{
				if(path.startsWith(rule.path))
				{
					return rule.mode;
				}
			}
		}
		return RepoMode.READ_ONLY;
	}
	public boolean isRepoUpdatable(ClientQuery q) {
		return getMode(q)==RepoMode.UPDATE;
	}
	public boolean isRepoAddable(ClientQuery q) {
		RepoMode m=getMode(q);
		return m==RepoMode.ADD_ONLY||m==RepoMode.UPDATE;
	}

}
