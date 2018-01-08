package hu.qgears.repocache.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;
import hu.qgears.repocache.AbstractRepoPlugin;
import hu.qgears.repocache.AbstractRepoPluginSubTree;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.p2plugin.ReplaceP2Plugin;

public class AccessRules {
	private static Log log=LogFactory.getLog(AccessRules.class);
	private File configFolder;
	private String accessRules="#Empty access rules config";
	private String clientAliasConfig="#Empty client alias config";
	private String pluginsConfig="#Empty plugins config";
	private Object syncObject=new Object();
	private List<AccessRule> rules=new ArrayList<>();
	private List<PluginDef> plugins=new ArrayList<>();
	private List<Pair<String, String>> clientAlias=new ArrayList<>();
	private List<Pair<String, String>> internetAlias=new ArrayList<>();
	private final String pathAccess="access.config";
	private final String pathClientAlias="client-alias.config";
	private final String pathPluginsConfig="plugins.config";
	private final String pathNameConfig="name.config";
	private String name="Unconfigured repocache name";
	public final UtilEvent<AccessRules> configChanged=new UtilEvent<>();
	public class PluginDef
	{
		public String pluginId;
		public String path;
		public String params;
		public AbstractRepoPlugin plugin;
		public PluginDef(String pluginId, String path, String params, AbstractRepoPlugin plugin) {
			super();
			this.pluginId = pluginId;
			this.path = path;
			this.params = params;
			this.plugin = plugin;
		}
	}
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
	public String getAccessRules() {
		synchronized (syncObject) {
			return accessRules;
		}
	}
	public AccessRules(File configFolder)
	{
		this.configFolder=configFolder;
		try {
			setAccessRules(UtilFile.loadAsString(new File(configFolder, pathAccess)));
		} catch (Exception e) {
			log.error("Loading initial version of access rules file");
		}
		try {
			setClientAlias(UtilFile.loadAsString(new File(configFolder, pathClientAlias)));
		} catch (Exception e) {
			log.error("Loading initial version of client alias file");
		}
		try {
			setPluginsConfig(UtilFile.loadAsString(new File(configFolder, pathPluginsConfig)));
		} catch (Exception e) {
			log.error("Loading initial version of client alias file");
		}
		try {
			setName(UtilFile.loadAsString(new File(configFolder, pathNameConfig)));
		} catch (Exception e) {
			log.error("Loading initial version of name file");
		}
	}
	public void saveClientAlias(String config) throws IOException
	{
		synchronized (syncObject) {
			UtilFile.saveAsFile(new File(configFolder, pathClientAlias), config);
			setClientAlias(config);
		}
	}
	private void setClientAlias(String config) {
		synchronized (syncObject) {
			log.info("Client aliases reloaded");
			this.clientAliasConfig=config;
			clientAlias.clear();
			List<String> lines=UtilString.split(config, "\r\n");
			for(String line:lines)
			{
				List<String> pieces=UtilString.split(line, "\t ");
				if(pieces.size()==2)
				{
					String from=pieces.get(0);
					String to=pieces.get(1);
					Pair<String, String> alias=new Pair<String, String>(from, to);
					clientAlias.add(alias);
					log.info("Alias added: '"+alias.getA()+"' '"+alias.getB()+"'");
				}else
				{
					// Log omit
				}
			}
		}
		configChanged.eventHappened(this);
	}
	public void saveAccessRules(String config) throws IOException
	{
		synchronized (syncObject) {
			UtilFile.saveAsFile(new File(configFolder, pathAccess), config);
			setAccessRules(config);
		}
	}
	private void setAccessRules(String config)
	{
		synchronized (syncObject) {
			log.info("Access rules reloaded");
			this.accessRules=config;
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
		configChanged.eventHappened(this);
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
		String path=q.getPathString();
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
	public String getInternetAliases() {
		synchronized(syncObject)
		{
			return "Internet aliases";
		}
	}
	public String getClientAlias() {
		synchronized (syncObject) {
			return clientAliasConfig;
		}
	}
	public String getPluginsConfig() {
		synchronized (syncObject) {
			return pluginsConfig;
		}
	}
	public void savePluginsConfig(String pluginsconfig) throws IOException
	{
		synchronized(syncObject)
		{
			UtilFile.saveAsFile(new File(configFolder, pathPluginsConfig), pluginsconfig);
			setPluginsConfig(pluginsconfig);
		}
	}
	private void setPluginsConfig(String pluginsconfig)
	{
		synchronized (syncObject) {
			this.pluginsConfig=pluginsconfig;
			plugins.clear();
			List<String> lines=UtilString.split(pluginsconfig, "\r\n");
			for(String line:lines)
			{
				List<String> pieces=UtilString.split(line, "\t ");
				if(pieces.size()>=2)
				{
					String pluginid=pieces.get(0);
					if(!pluginid.startsWith("#"))
					{
						String path=pieces.get(1);
						String additional=line.substring(line.indexOf(pluginid)+pluginid.length());
						additional=additional.substring(additional.indexOf(path)+path.length());
						if(!path.startsWith("/"))
						{
							path="/"+path;
						}
						AbstractRepoPluginSubTree plugin=null;
						switch (pluginid) {
						case "replace-p2":
							plugin=new ReplaceP2Plugin();
							break;
						default:
							break;
						}
						if(plugin==null)
						{
							log.error("Unknown plugin type: '"+pluginid+"'");
						}else
						{
							PluginDef pd=new PluginDef(pluginid, path, additional, plugin);
							plugin.init(pd);
							plugins.add(pd);
							log.info("Plugin: "+pluginid+" '"+path+"'"+" "+additional);
						}
					}
				}else
				{
					// Log omit
				}
			}
		}
		configChanged.eventHappened(this);
	}
	public Path rewriteClientPath(Path path) {
		return rewritePath(clientAlias, path, "Client");
	}
	public Path rewritePath(List<Pair<String,String>> aliases, Path path, String name)
	{
		boolean changed=false;
		String p="/"+path.toStringPath();
		synchronized(syncObject)
		{
			for(Pair<String, String> alias: aliases)
			{
				if(p.startsWith(alias.getA()))
				{
					String newP=alias.getB()+p.substring(alias.getA().length());
					log.info(name+" alias rewrite: "+p+"->"+newP);
					p=newP;
				}
			}
		}
		if(changed)
		{
			return new Path(p);
		}
		return path;

	}
	public Path rewriteInternetPath(Path path) {
		return rewritePath(internetAlias, path, "Internet");
	}
	public String getName() {
		synchronized (syncObject) {
			return name;
		}
	}
	public void saveName(String name) throws IOException {
		synchronized (syncObject) {
			UtilFile.saveAsFile(new File(configFolder, pathNameConfig), name);
			setName(name);
		}
	}
	private void setName(String name) {
		synchronized (syncObject) {
			this.name=name;
		}
		configChanged.eventHappened(this);
	}
	public PluginDef getPluginDef(Path path) {
		String p="/"+path.toStringPath();
		synchronized (syncObject) {
			for(PluginDef pd: plugins)
			{
				if(pd.path.startsWith(p))
				{
					return pd;
				}
			}
		}
		return null;
	}

}
