package hu.qgears.repocache.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.HttpConfiguration;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;
import hu.qgears.repocache.AbstractRepoPlugin;
import hu.qgears.repocache.AbstractRepoPluginSubTree;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.p2plugin.ReplaceP2Plugin;

public class RepoConfiguration {
	/** 
	 * Abbreviation for 'read only' mode to be used in access rule descriptions.
	 * @see RepoMode#READ_ONLY 
	 */
	private static final String REPOMODE_ABBR_RO = "ro";
	/**
	 * Abbreviation for 'add only' mode to be used in access rule descriptions.
	 * @see RepoMode#ADD_ONLY 
	 */
	private static final String REPOMODE_ABBR_ADD_ONLY = "add";
	/**
	 * Abbreviation for 'update' mode to be used in access rule descriptions.
	 * @see RepoMode#UPDATE
	 */
	private static final String REPOMODE_ABBR_UPDATE = "update";
	/**
	 * Abbreviation for 'transparent' mode to be used in access rule descriptions.
	 * @see RepoMode#NO_CACHE_TRANSPARENT
	 */
	private static final String REPOMODE_ABBR_TRANSPARENT = "transparent";
	private static Log log=LogFactory.getLog(RepoConfiguration.class);
	private File configFolder;
	private String accessRules="#Empty access rules config";
	private String clientAliasConfig="#Empty client alias config";
	private String pluginsConfig="#Empty plugins config";
	private Object syncObject=new Object();
	
	private List<AccessRule> rules=new ArrayList<>();
	private List<PluginDef> plugins=new ArrayList<>();
	
	private List<Pair<String, String>> clientAlias=new ArrayList<>();
	private List<Pair<String, String>> internetAlias=new ArrayList<>();
	
	/**
	 * @see HttpConfiguration#se
	 */
	protected int httpSoTimeoutMs = 0;
	protected int httpConnectionTimeoutMs = 0;
	
	/** Upstream proxy server hostname or IP address - optional */
	private String upstreamProxyHostname;
	/** Upstream proxy server port - optional */
	private Integer upstreamProxyPort;
	
	public static final String ACCESS_RULE_CONFIG_FILE = "access.config";
	private final String pathClientAlias="client-alias.config";
	private final String pathPluginsConfig="plugins.config";
	/**
	 * Textual configuration file containing the following options:
	 * <ul>
	 * <li>{@value #PROP_NAME_BROWSER_TITLE}
	 * <li>{@value #PROP_NAME_HTTP_CONN_TIMEOUT}
	 * <li>{@value #PROP_NAME_HTTP_SO_TIMEOUT}
	 * </ul>
	 */
	private final String pathNameConfig="repocache.config";
	
	/**
	 * Option name for title of administration page in a web user interface.
	 */
	private static final String PROP_NAME_BROWSER_TITLE = "browser.title";
	/**
	 * Option name for HTTP socket timeout for downloading artifacts.
	 */
	private static final String PROP_NAME_HTTP_SO_TIMEOUT = "http.sotimeout";
	/**
	 * Option name for HTTP connection timeout for downloading artifacts.
	 */
	private static final String PROP_NAME_HTTP_CONN_TIMEOUT = "http.conntimeout";
	/**
	 * Title of administration page in a web user interface.
	 */
	private String name = "Unconfigured repocache name";
	
	/**
	 * @see #upstreamProxyHostname
	 */
	private static final String PROP_NAME_UPSTREAM_PROXY_HOST = "upstreamproxy.hostname";
	/**
	 * @see #upstreamProxyPort
	 */
	private static final String PROP_NAME_UPSTREAM_PROXY_PORT = "upstreamproxy.port";
	
	public final UtilEvent<RepoConfiguration> configChanged=new UtilEvent<>();
	
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
	
	/**
	 * Creates an instance of repocache configuration, loading options from
	 * configuration files.
	 * @param configFolder the directory containing the configuration files
	 * @throws IOException if any IO-related exception occurs during loading the
	 * configuration files 
	 */
	public RepoConfiguration(File configFolder)
	{
		this.configFolder=configFolder;
		try {
			setAccessRules(UtilFile.loadAsString(new File(configFolder, ACCESS_RULE_CONFIG_FILE)));
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
			loadFromFile();
		} catch (Exception e) {
			log.error("Could not load properties file; starting with defaults.");
		}
	}
	
	/**
	 * Loads part of configuration from the default properties file. 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private Properties loadFromFile() throws IOException {
		synchronized (syncObject) {
			try (final FileReader cfgStream = new FileReader(
					new File(configFolder, pathNameConfig))) {
				final Properties repoCacheProps = new Properties();
				
				repoCacheProps.load(cfgStream);
				
				this.name = repoCacheProps.getProperty(PROP_NAME_BROWSER_TITLE, name);
				this.httpConnectionTimeoutMs = Integer.parseInt(repoCacheProps.getProperty(
						PROP_NAME_HTTP_CONN_TIMEOUT, "0"));
				this.httpSoTimeoutMs = Integer.parseInt(repoCacheProps.getProperty(
						PROP_NAME_HTTP_SO_TIMEOUT, "0"));
				this.upstreamProxyHostname = repoCacheProps.getProperty(
						PROP_NAME_UPSTREAM_PROXY_HOST);
				final String upstreamProxyPortString = repoCacheProps.getProperty(
						PROP_NAME_UPSTREAM_PROXY_PORT);
				this.upstreamProxyPort = upstreamProxyPortString == null
						? null : Integer.parseInt(upstreamProxyPortString);
				
				return repoCacheProps;
			}
		}
	}
	
	/**
	 * Overwrites the configuration file with current data, preserving comments.
	 * @see #pathNameConfig
	 */
	public void saveToFile() throws IOException {
		synchronized (syncObject) {
			final Properties repoCacheProps = new Properties();
			final File configFile = new File(configFolder, pathNameConfig);
			
			/* 
			 * Options will be loaded first, not to lose content from the 
			 * configuration file.
			 */
			if (configFile.exists() && configFile.isFile()) {
				try (final FileReader cfgReader = new FileReader(configFile)) {
					repoCacheProps.load(cfgReader);
				}
			}
			
			repoCacheProps.setProperty(PROP_NAME_HTTP_CONN_TIMEOUT, 
					Integer.toString(httpConnectionTimeoutMs));
			repoCacheProps.setProperty(PROP_NAME_HTTP_SO_TIMEOUT, 
					Integer.toString(httpSoTimeoutMs));
			repoCacheProps.setProperty(PROP_NAME_BROWSER_TITLE, name);
			
			if (isUpstreamProxyConfigured()) {
				repoCacheProps.setProperty(PROP_NAME_UPSTREAM_PROXY_HOST, 
						upstreamProxyHostname);
				repoCacheProps.setProperty(PROP_NAME_UPSTREAM_PROXY_PORT, 
						Integer.toString(upstreamProxyPort));
			}
			
			try (final FileWriter cfgWriter = new FileWriter(configFile)) {
				repoCacheProps.store(cfgWriter, null);
			}
		}
	}
	
	public void saveClientAlias(String config) throws IOException
	{
		synchronized (syncObject) {
			saveWithMkDirParent(new File(configFolder, pathClientAlias), config);
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
			saveWithMkDirParent(new File(configFolder, ACCESS_RULE_CONFIG_FILE), config);
			setAccessRules(config);
		}
	}
	
	/**
	 * Sets up the access rules by a composite textual description. Access rules
	 * description is a single or multiline text, in which a line consists of
	 * the following tokens:
	 * <ol>
	 * <li>access mode; possible values are: {@value #REPOMODE_ABBR_RO}, 
	 * {@value #REPOMODE_ABBR_ADD_ONLY}, {@value #REPOMODE_ABBR_UPDATE} and
	 * {@value #REPOMODE_ABBR_TRANSPARENT}
	 * <li>tabulator character as separator
	 * <li>an URL path prefix, without the {@code http://} or {@code https://}
	 * protocol string. If a request URL, received by the repocache proxy, 
	 * starts with a matching prefix, the specified repo access mode will be applied to it.
	 * </ol> 
	 * @param config the textual description of access rules
	 */
	public void setAccessRules(String config)
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
				} else 
				{
					log.error("Access rule skipped because of too many tokens: "
							+ "\"" + line + "\"");
				}
			}
		}
		configChanged.eventHappened(this);
	}
	private RepoMode parseMode(String mode) {
		switch (mode) {
		case REPOMODE_ABBR_RO:
			return RepoMode.READ_ONLY;
		case REPOMODE_ABBR_ADD_ONLY:
			return RepoMode.ADD_ONLY;
		case REPOMODE_ABBR_UPDATE:
			return RepoMode.UPDATE;
		case REPOMODE_ABBR_TRANSPARENT:
			return RepoMode.NO_CACHE_TRANSPARENT;
		default:
			log.error("Unknown repository access mode in configuration: " + mode);
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
			saveWithMkDirParent(new File(configFolder, pathPluginsConfig), pluginsconfig);
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
					changed=true;
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
	
	public void saveOption(final String optionName, final String value) {
		
	}
	
	public void saveName(String name) throws IOException {
		synchronized (syncObject) {
			setName(name);
			saveToFile();
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
				if(p.startsWith(pd.path))
				{
					return pd;
				}
			}
		}
		return null;
	}
	
	public int getHttpConnectionTimeoutMs() {
		return httpConnectionTimeoutMs;
	}
	
	public void setHttpConnectionTimeoutMs(final int httpConnectionTimeoutMs) {
		this.httpConnectionTimeoutMs = httpConnectionTimeoutMs;
	}
	
	public int getHttpSoTimeoutMs() {
		return httpSoTimeoutMs;
	}
	
	public void setHttpSoTimeoutMs(final int httpSoTimeoutMs) {
		this.httpSoTimeoutMs = httpSoTimeoutMs;
	}

	private void saveWithMkDirParent(File targetFile, String content) throws IOException{
		if (!targetFile.getParentFile().exists()){
			if (!targetFile.getParentFile().mkdirs()){
				throw new IOException("Cannot create directory: "+targetFile.getParent());
			}
		}
		UtilFile.saveAsFile(targetFile, content);
		
	}

	public String getUpstreamProxyHostname() {
		return upstreamProxyHostname;
	}

	public Integer getUpstreamProxyPort() {
		return upstreamProxyPort;
	}
	
	public boolean isUpstreamProxyConfigured() {
		return upstreamProxyHostname != null && !upstreamProxyHostname.isEmpty()
				&& upstreamProxyPort != null;
	}
}
