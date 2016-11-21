package hu.qgears.repocache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.UtilFile;
import hu.qgears.repocache.httpget.HttpClientToInternet;
import joptsimple.annot.AnnotatedClass;

public class RepoCache {
	private Repository repository;
	public Git git;
	HttpClientToInternet client = new HttpClientToInternet();
	public static final String maintenancefilesprefix = "XXXRepoCache.";
	private static Logger log=LoggerFactory.getLogger(RepoCache.class);
	private List<AbstractRepoPlugin> plugins=new ArrayList<>();
	private ReadConfig configuration;
	private CommitTimer commitTimer;
	private File worktree;
	private String repoVersion="Repo Cace 1.0.0 by Q-Gears Kft.";
	private String versionFilePath="version.txt";

	public static void main(String[] args) throws Exception {
		CommandLineArgs clargs=new CommandLineArgs();
		AnnotatedClass cl = new AnnotatedClass();
		cl.parseAnnotations(clargs);
		System.out.println("Repository cache program. Usage:\n");
		cl.printHelpOn(System.out);
		cl.parseArgs(args);
		String parsedOptions=cl.optionsToString();
		log.info(parsedOptions);
		clargs.validate();
		ReadConfig config=new ReadConfig(clargs);
		RepoCache rc = new RepoCache(config);
		rc.start();
	}
	public RepoCache(ReadConfig configuration) {
		this.configuration=configuration;
	}

	public void start() throws Exception {
		commitTimer=new CommitTimer(this);
		File wc=configuration.getLocalGitRepo();
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
		System.out.println("Folder: " + repository.getDirectory());
		System.out.println("Branch: " + repository.getBranch());
		System.out.println("IsBare: " + repository.isBare());
		worktree=git.getRepository().getWorkTree();
		assertStatusClean();
		try (ObjectReader reader = repository.newObjectReader()) {
			for (RevCommit rc : git.log().addPath("alma.txt").call()) {
				System.out.println("revcommit: " + rc.getFullMessage());
				// .. and narrow it down to the single file's path
				TreeWalk treewalk = TreeWalk.forPath(reader, "alma.txt", rc.getTree());
				if (treewalk != null) {
					// use the blob id to read the file's data
					byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
					System.out.println("file content: '" + new String(data, "utf-8") + "'");
				} else {
				}
			}
		}
		plugins.add(new RepoPluginP2(this));
		plugins.add(new RepoPluginHttp(this));
		plugins.add(new RepoPluginMaven(this));

		Server server = new Server(configuration.getCommandLine().port);
		server.setHandler(new RepoHandler(this));
		server.start();
		server.join();
	}

	public void assertStatusClean() throws IOException, NoWorkTreeException, GitAPIException {
		synchronized (this) {
			Status status=git.status().call();
			log.info("Git status clean: "+status.isClean());
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

	private QueryResponse loadFromCache(Path path, boolean folder) throws IOException {
		if(path.pieces.size()==0)
		{
			return null;
		}
		byte[] file=getFile(path);
		byte[] fileMeta=getFile(getMeta(path));
		if(file!=null && fileMeta!=null)
		{
			// Folder
			return QueryResponse.createFromContentAndMeta(file, fileMeta, folder);
		}
		return null;
	}

	private void updateFile(Path path, QueryResponse response) throws IOException, NoFilepatternException, GitAPIException {
		deleteIfExists(path);
		deleteIfExists(getMeta(path));
		deleteIfExists(getFolderListingPath(path));
		deleteIfExists(getMeta(getFolderListingPath(path)));
		if(response!=null)
		{
			if(response.folder)
			{
				path=getFolderListingPath(path);
			}
			Path meta=getMeta(path);
			getWorkingCopyFile(path).getParentFile().mkdirs();
			UtilFile.saveAsFile(getWorkingCopyFile(path), response.openInputStream());
			UtilFile.saveAsFile(getWorkingCopyFile(meta), response.createMeta());
			String message=response.folder?"Auto update folder listing: "+new Path(path).remove(path.pieces.size()-1).setFolder(true).toStringPath():("Auto update path: "+path.toStringPath());
			commitTimer.addCommit(message);
			log.info("Path refreshed in cache: "+response+" to git: "+path.toStringPath());
		}else
		{
			log.info("Path deleted on remote: "+response+" retained in git: "+path.toStringPath());
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

	private byte[] getFile(Path path) throws IOException {
		File f=getWorkingCopyFile(path);
		if(!f.exists()||f.isDirectory())
		{
			return null;
		}
		return UtilFile.loadFile(f);
	}

	private Path getFolderListingPath(Path localPath) {
		return new Path(localPath).add(maintenancefilesprefix + "listing");
	}

	/**
	 * Get Metadata file URL
	 * @param path
	 * @return
	 */
	private Path getMeta(Path p) {
		if(p.pieces.size()==0)
		{
			return null;
		}
		p=new Path(p);
		String name=p.pieces.get(p.pieces.size()-1);
		p.pieces.set(p.pieces.size()-1, maintenancefilesprefix+"meta."+name);
		return p;
	}

	
//	private byte[] getFile(ObjectReader reader, Path path)
//			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
//		ObjectId lastCommitId = repository.resolve(Constants.HEAD);
//
//		// a RevWalk allows to walk over commits based on some filtering that is
//		// defined
//		try (RevWalk revWalk = new RevWalk(repository)) {
//			RevCommit commit = revWalk.parseCommit(lastCommitId);
//			// and using commit's tree find the path
//			RevTree tree = commit.getTree();
////			System.out.println("Having tree: " + tree);
//			return getFile(reader, path, tree);
//		}
//	}
//	private byte[] getFile(ObjectReader reader, Path path, AnyObjectId tree)
//			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
//		// now try to find a specific file
//		try (TreeWalk treeWalk = new TreeWalk(repository)) {
//			treeWalk.addTree(tree);
//			treeWalk.setRecursive(true);
//			treeWalk.setFilter(PathFilter.create(path.toStringPath()));
//			if (!treeWalk.next()) {
//				return null;
//			}
//	
//			ObjectId objectId = treeWalk.getObjectId(0);
//			ObjectLoader loader = repository.open(objectId);
//			return loader.getBytes();
//		}
//	}
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
			// TODO Re-read cached content to check if it was already updated?
			// cachedContent=getCache(path);
			if(qr!=null&&!(qr.equals(cachedContent)))
			{
				updateFile(path, qr);
			}else
			{
				log.info("Path did not change: "+path.toStringPath());
			}
		}
	}

	/**
	 * Handles update strategy.
	 * @param q
	 * @param cachedContent
	 * @return true means that the remote server should be queried for an update.
	 */
	public boolean updateRequired(ClientQuery q, QueryResponse cachedContent) {
		if(configuration.getCommandLine().localOnly)
		{
			return false;
		}
		ClientSetup client=getConfiguration().getClientSetup(q.getClientIdentifier());
		if(cachedContent!=null)
		{
			return client.isUpdater();
		}
		return client.isAdder();
	}
	public ReadConfig getConfiguration() {
		return configuration;
	}
	public CommitTimer getCommitTimer() {
		return commitTimer;
	}
	public File createTmpFile(Path path) {
		// TODO Auto-generated method stub
		return null;
	}
}
