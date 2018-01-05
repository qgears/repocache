package hu.qgears.repocache;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import hu.qgears.commons.UtilFile;
import hu.qgears.repocache.config.ClientSetup;
import hu.qgears.repocache.config.ReadConfig;
import hu.qgears.repocache.config.RepoModeHandler;
import hu.qgears.repocache.handler.MyRequestHandler;
import hu.qgears.repocache.handler.ProxyRepoHandler;
import hu.qgears.repocache.handler.RepoHandler;
import hu.qgears.repocache.httpget.StreamingHttpClient;
import hu.qgears.repocache.httpplugin.RepoPluginHttp;
import hu.qgears.repocache.httpplugin.RepoPluginProxy;
import hu.qgears.repocache.https.DynamicSSLProxyConnector;
import hu.qgears.repocache.https.HttpsProxyServer;
import hu.qgears.repocache.mavenplugin.RepoPluginMaven;
import hu.qgears.repocache.p2plugin.P2VersionFolderUtil;
import hu.qgears.repocache.p2plugin.RepoPluginP2;
import joptsimple.annot.AnnotatedClass;

public class RepoCache {
	private Repository repository;
	public Git git;
	public StreamingHttpClient client = new StreamingHttpClient();
	public static final String maintenancefilesprefix = "XXXRepoCache.";
	private static Log log=LogFactory.getLog(RepoCache.class);
	private List<AbstractRepoPlugin> plugins=new ArrayList<>();
	private ReadConfig configuration;
	private RepoModeHandler repoModeHandler;
	private CommitTimer commitTimer;
	private File worktree;
	private String repoVersion="Repo Cache 1.0.0 by Q-Gears Kft.\n";
	private String versionFilePath="version.txt";

	public static void main(String[] args) throws Exception {
		CommandLineArgs clargs=new CommandLineArgs();
		AnnotatedClass cl = new AnnotatedClass();
		cl.parseAnnotations(clargs);
		System.out.println("Repository cache program. Usage:\n");
		cl.printHelpOn(System.out);
		cl.parseArgs(args);
		if(clargs.log4jToConsole)
		{
			ConsoleAppender console = new ConsoleAppender(); //create appender
			//configure the appender
			String PATTERN = "%d [%p|%c|%C{1}] %m%n";
			console.setLayout(new PatternLayout(PATTERN)); 
			console.setThreshold(Level.ALL);
			console.activateOptions();
			//add appender to any Logger (here is root)
			Logger.getRootLogger().addAppender(console);
		}
		String parsedOptions=cl.optionsToString();
		log.info("Options:\n" + parsedOptions);
		clargs.validate();
		ReadConfig config=new ReadConfig(clargs);
		RepoModeHandler repoModeH = new RepoModeHandler(clargs);
		RepoCache rc = new RepoCache(config, repoModeH);
		rc.start();
	}
	public RepoCache(ReadConfig configuration, RepoModeHandler repoModeHandler) {
		this.configuration=configuration;
		this.repoModeHandler=repoModeHandler;
	}

	public void start() throws Exception {
		// register Message as shutdown hook
		Runtime.getRuntime().addShutdownHook(new RepoShutdown());

		commitTimer=new CommitTimer(this);
		File wc=configuration.getLocalGitRepo();
		new P2VersionFolderUtil(wc.getAbsolutePath());
		if(!wc.exists())
		{
			wc.mkdirs();
			try(Git git = Git.init().setDirectory( wc ).call())
			{
				File initial=new File(wc, versionFilePath);
				UtilFile.saveAsFile(initial, repoVersion);
				git.add().addFilepattern(".").call();
				git.commit().setMessage("Initial commit").call();
			}
		}
		git = Git.open(wc);
		repository=git.getRepository();
		log.info("Git params: folder: " + repository.getDirectory() + ", branch: " + repository.getBranch() + ", isBare: " + repository.isBare());
		worktree=git.getRepository().getWorkTree();
		assertStatusClean();
		try (ObjectReader reader = repository.newObjectReader()) {
			for (RevCommit rc : git.log().addPath("alma.txt").call()) {
				log.debug("revcommit: " + rc.getFullMessage());
				// .. and narrow it down to the single file's path
				TreeWalk treewalk = TreeWalk.forPath(reader, "alma.txt", rc.getTree());
				if (treewalk != null) {
					// use the blob id to read the file's data
					byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
					log.debug("file content: '" + new String(data, "utf-8") + "'");
				} else {
				}
			}
		}
		RepoHandler rh = new RepoHandler(this);
		plugins.add(new RepoPluginP2(this, rh));
		plugins.add(new RepoPluginHttp(this));
		plugins.add(new RepoPluginMaven(this));

		CommandLineArgs args=configuration.getCommandLine();
		if (args.hasProxyPortDefined()) {
			startProxyServer(args.serverHost, args.getProxyPortReadonly(), new ProxyRepoHandler(RepoCache.this));	// READ_ONLY mode
			startProxyServer(args.serverHost,args.getProxyPortUpdate(), new ProxyRepoHandler(RepoCache.this).setUpdateProxyPort(true));// UPDATE mode
			plugins.add(new RepoPluginProxy(this));
		}

		if (configuration.getCommandLine().hasHttpsProxyPortDefined()) {
			String localHost="127.0.0.1";
			int readOnlyPlaintext=startProxyServer(localHost, 0, new ProxyRepoHandler(RepoCache.this).setHttps(true));	// READ_ONLY mode
			int updatePlaintext=startProxyServer(localHost, 0, new ProxyRepoHandler(RepoCache.this).setHttps(true));// UPDATE mode
			DynamicSSLProxyConnector creadonly=new DynamicSSLProxyConnector(args, localHost, readOnlyPlaintext);
			DynamicSSLProxyConnector cupdate=new DynamicSSLProxyConnector(args, localHost, updatePlaintext);
			new HttpsProxyServer(args, args.serverHost, args.getHttpsProxyPortReadonly(), creadonly).start();
			new HttpsProxyServer(args, args.serverHost, args.getHttpsProxyPortUpdate(), cupdate).start();
		}
		
		Server server = new Server(configuration.getCommandLine().port);
		server.setHandler(rh);
		server.start();
		log.info("RepoCache started....");
		
//		Server s2=new Server();
//        ServerConnector connector=new ServerConnector(s2);
//        connector.setHost(args.getServerHost());
//        connector.setPort(9999);
//        s2.addConnector(connector);
//		s2.start();
//		System.out.println(" s2 connectors: "+s2.getConnectors().length+" "+s2.getConnectors()[0]);

		
		server.join();
		
	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param prh
	 * @return the port on which the server has started.
	 * @throws Exception
	 */
	private int startProxyServer(String host, int port, MyRequestHandler prh) throws Exception
	{
		Server proxyServer = new Server(new InetSocketAddress(host, port));
		proxyServer.setHandler(prh);
		proxyServer.start();
		ServerConnector sc=(ServerConnector)proxyServer.getConnectors()[0];
		return sc.getLocalPort();
	}
	
	public void assertStatusClean() throws IOException, NoWorkTreeException, GitAPIException {
		synchronized (this) {
			Status status=git.status().call();
			log.debug("Git status clean: "+status.isClean());
			if(!status.isClean())
			{
				throw new IOException("git repo is not clean");
			}
			String storedVersion=UtilFile.loadAsString(new File(worktree, versionFilePath));
			if(!storedVersion.equals(repoVersion))
			{
				throw new IOException("Invalid repocache version: "+storedVersion);
			}
		}
	}

	public QueryResponse getCache(Path path) throws IOException
	{
		synchronized (this) {
			QueryResponse listingResponse=loadFromCache(getFolderListingPath(path), true);
			QueryResponse fileResponse=loadFromCache(path, false);
			if(listingResponse!=null)
			{
				listingResponse.fileSystemFolder=getWorkingCopyFile(path);
				// Folder
				return listingResponse;
			}else if(fileResponse!=null)
			{
				// Normal file
				return fileResponse;
			}else
			{
				return null;
			}
		}
	}

	public QueryResponse loadDirFromCache(Path path) throws IOException {
		if(path.pieces.size()==0) {
			return null;
		}
		File file=getDir(path);
		if (file != null) {
			QueryResponse qr = new QueryResponseFile(path.toStringPath(), file, true);
			qr.fileSystemFolder=getWorkingCopyFile(path);
			return qr;
		}
		return null;
	}
	
	private QueryResponse loadFromCache(Path path, boolean folder) throws IOException {
		if(path.pieces.size()==0)
		{
			return null;
		}
		File file=getFile(path);
		if(file!=null)
		{
			return new QueryResponseFile(path.toStringPath(), file, folder);
		}
		return null;
	}

	private void updateFile(Path path, QueryResponse response) throws IOException, NoFilepatternException, GitAPIException {
		deleteIfExists(path);
		deleteIfExists(getFolderListingPath(path));
		if(response!=null)
		{
			if(response.folder)
			{
				path=getFolderListingPath(path);
			}
			getWorkingCopyFile(path).getParentFile().mkdirs();
			response.saveToFile(getWorkingCopyFile(path));
			String message=response.folder?"Auto update folder listing: "+new Path(path).remove(path.pieces.size()-1).setFolder(true).toStringPath():("Auto update path: "+path.toStringPath());
			commitTimer.addCommit(message);
			log.info("Path refreshed in cache: "+response+" to git: "+path.toStringPath());
		}else
		{
			log.info("Path deleted on remote: "+response+" retained in git: "+path.toStringPath());
		}
	}

	/**
	 * Creates a file on the given local path with missing folders.
	 * @param path
	 * @param content
	 * @param commitMsg
	 * @throws IOException
	 */
	public void createFile (Path path, byte[] content, String commitMsg) throws IOException {
		synchronized (this) {
			deleteIfExists(path);
			File f=getWorkingCopyFile(path);
			f.getParentFile().mkdirs();
			UtilFile.saveAsFile(f, content);
			commitTimer.addCommit(commitMsg + path.toStringPath());
		}
	}
	
	private void deleteIfExists(Path path) {
		if(path==null)
		{
			return;
		}
		File f=getWorkingCopyFile(path);
		if(f.exists())
		{
			f.delete();
		}
	}

	private File getWorkingCopyFile(Path path) {
		return new File(worktree, path.toStringPath());
	}

	private File getFile(Path path) throws IOException {
		File f=getWorkingCopyFile(path);
		if(!f.exists()||f.isDirectory())
		{
			return null;
		}
		return f;
	}

	private File getDir(Path path) throws IOException {
		File f=getWorkingCopyFile(path);
		if(!f.exists()||!f.isDirectory())
		{
			return null;
		}
		return f;
	}

	private Path getFolderListingPath(Path localPath) {
		return new Path(localPath).add(maintenancefilesprefix + "listing");
	}

	public List<AbstractRepoPlugin> getPlugins()
	{
		return plugins;
	}
	/**
	 * Update the stored cached content.
	 * @param path path to overwrite.
	 * @param cachedContent Old content to be overwritten.
	 * @param qr new value to store. null is not stored, we do not delete existing files.
	 * @throws NoFilepatternException
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public void updateResponse(Path path, QueryResponse cachedContent, QueryResponse qr) throws NoFilepatternException, IOException, GitAPIException {
		synchronized (this) {
			if(qr!=null&&!(qr.equals(cachedContent)))
			{
				updateFile(path, qr);
			}else
			{
				log.debug("Path did not change: "+path.toStringPath());
			}
		}
	}

	/**
	 * Handles update strategy.
	 * @param q
	 * @param cachedContent
	 * @return true means that the remote server should be queried for an update.
	 */
	public boolean updateRequired(ClientQuery q, QueryResponse cachedContent, boolean updaterProxyPort) {
		if(configuration.getCommandLine().localOnly) {
			return false;
		}

		boolean updRequired = true;
		// Update req by repo
		String repoName = null;
		if (q.path.pieces.size() > 0 && "proxy".equals(q.path.pieces.get(0))) {
			repoName = "proxy-repo";
		} else if (q.path.pieces.size() > 1) {
			repoName = q.path.pieces.get(1);
		}
		if (repoName != null) {
			if (cachedContent!=null) {
				updRequired = getRepoModeHandler().isRepoUpdatable(repoName);
			} else {
				updRequired = getRepoModeHandler().isRepoAddable(repoName);
			}
		}
		
		// Update req enabled by client
		if(updRequired) {
			if (q.path.pieces.size() > 0 && "proxy".equals(q.path.pieces.get(0))) {
				updRequired = (configuration.getCommandLine().hasProxyPortDefined() && updaterProxyPort);
			} else {
				ClientSetup client=getConfiguration().getClientSetup(q.getClientIdentifier());
				updRequired = !client.isReadonly();
			}
		}
		log.debug("Update required for path: " + q.path + "=" + updRequired);
		return updRequired;
	}
	public ReadConfig getConfiguration() {
		return configuration;
	}
	public RepoModeHandler getRepoModeHandler() {
		return repoModeHandler;
	}
	public CommitTimer getCommitTimer() {
		return commitTimer;
	}
	private AtomicInteger ctr=new AtomicInteger(0);
	public File createTmpFile(Path path) {
		return new File(configuration.getCommandLine().downloadsFolder, ""+ctr.incrementAndGet());
	}

	/**
	 * Shutdown hook
	 * When system exit, make an explicite commit on git. (Git stays clean)
	 * @author akos
	 *
	 */
	class RepoShutdown extends Thread {
		public void run() {
			log.info("ShutdownHook Bye!");
			try {
				commitTimer.executeCommit();
			} catch (Exception e) {
				log.error("Error commiting in shutdown hook!", e);
			}
		}
	}

}
