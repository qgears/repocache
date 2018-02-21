package hu.qgears.repocache.p2plugin;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import hu.qgears.repocache.AbstractRepoPluginSubTree;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.RepoCache;
import hu.qgears.repocache.config.RepoConfiguration.PluginDef;
import hu.qgears.repocache.httpget.HttpGet;

/**
 * P2 plugin that allows to auto create a composite p2 repository from an remote
 * single-level p2 repository.
 * <p>
 * The plugin will clone the remote repository automatically (the accessed
 * resources) into a sub repository. Each sub repository represents a given
 * version of the remote repository. The plugin automatically detects if the
 * latest version in cache differs from the remote content. If any change has
 * been detected, a new subrepository will be created, and the remote content
 * will be synchronized into this new folder. This way the old repo content will
 * not be overridden, but can be accessed via the root composite repository.
 * 
 * @author agostoni
 *
 */
public class ReplaceP2Plugin extends AbstractRepoPluginSubTree {

	private static final Logger LOG = Logger.getLogger(ReplaceP2Plugin.class);
	private static final String ROOT_INDEX = "p2.index";
	private static final String ROOT_CONT = "compositeContent.xml";
	private static final String ROOT_ARTI = "compositeArtifacts.xml";
	private static final int MAX_VERSIONS = 1000;

	private PluginDef pd;
	private RepoCache repoCache;

	@Override
	public void init(PluginDef pd) {
		this.pd = pd;
	}

	@Override
	public QueryResponse getOnlineResponse(Path fullPath, Path pluginLocalPath, ClientQuery q,
			QueryResponse cachedContent, boolean netAllowed) throws IOException {
		if (netAllowed) {
			this.repoCache = q.rc;
			Path repoRootPath = new Path(pd.path);
			Path relativeToRepoRoot = getRelativePath(repoRootPath, fullPath);
			if (relativeToRepoRoot.pieces.size() == 1) {
				// root repository, only content descriptors are returned
				String resource = relativeToRepoRoot.getFileName();
				if (ROOT_INDEX.equals(resource)) {
					if (cachedContent != null) {
						// root p2 index never changes, so if already exists
						// then nothing to do
						return cachedContent;
					} else {
						LOG.info("Accessing p2.index first time, generating descriptor...");
						return new P2Index(q).generate();
					}
				} else if (ROOT_ARTI.equals(resource) || ROOT_CONT.equals(resource)) {
					if (newVersionRequired()) {
						return regenerateXmls(q, resource);
					} else {
						return cachedContent;
					}
				} else {
					// other contents are not served
					return null;
				}
			} else if (relativeToRepoRoot.pieces.size() > 1) {
				if (cachedContent != null) {
					return cachedContent;
				} else {
					String resource = relativeToRepoRoot.getFileName();
					if (resource.contains("compositeContent.") || resource.contains("compositeArtifacts.")) {
						LOG.warn("Ignoring request on " + pluginLocalPath
								+ ". Composite repositories should not be used with this plugin!");
					} else {
						// redirect to real http url
						Integer v = getLatestVersionInCache();
						if (v != null) {
							String subrepo = getVersionSubfolder(v.intValue());
							String request = relativeToRepoRoot.pieces.get(0);
							if (subrepo.equals(request)) {
								return repoCache.client.get(new HttpGet(
										repoCache.createTmpFile(pluginLocalPath),
										getRealHttpPath(fullPath,subrepo),
										repoCache.getConfiguration()));
							} else {
								LOG.info("Attempting to acces uncached old version of " + fullPath.toStringPath());
								LOG.info(request
										+ " cannot be updated any more, because the repository content changed on remote server.");
							}
						} else {
							// inconsistency, v shouldn't be null here returning
							// nothing
							LOG.error("Attempt to acces sub repository before initializing root repo "
									+ fullPath.toStringPath());
						}
					}
				}
			}
		}
		return cachedContent;
	}

	private QueryResponse regenerateXmls(ClientQuery q, String resourceToReturn) throws IOException {
		Integer i = getLatestVersionInCache();
		int maxVersion;
		if (i == null) {
			// Starting version from 1
			maxVersion = 1;
		} else {
			maxVersion = 1 + i;
		}
		LOG.info("Creating new version of P2 repo " + pd.path + ". Version " + maxVersion);
		QueryResponse cc = new P2CompositeContent(q, maxVersion, "Generated P2 repo for " + pd.path).generate();
		QueryResponse ca = new P2CompositeArtifacts(q, maxVersion, "Generated P2 repo for " + pd.path).generate();
		try {
			repoCache.createDir(new Path(pd.path + "/" + getVersionSubfolder(maxVersion)),
					"Creating folder for new version " + maxVersion);
			if (ROOT_ARTI.equals(resourceToReturn)) {
				repoCache.updateResponse(q, new Path(pd.path + "/" + ROOT_CONT), null, cc);
				return ca;
			} else {
				repoCache.updateResponse(q, new Path(pd.path + "/" + ROOT_ARTI), null, ca);
				return cc;
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private boolean newVersionRequired() {
		Integer ver = getLatestVersionInCache();
		if (ver == null) {
			LOG.info("No versions exist in cache, new version will be created.");
			return true;
		} else {
			return checkContentChanged(ver);
		}
	}

	private boolean checkContentChanged(int currentVersion) {
		String versionSubfolder = getVersionSubfolder(currentVersion);
		Path vRoot = new Path(pd.path + "/" + versionSubfolder);
		boolean changed = false;
		for (Path p : repoCache.getExistingItemsInFolder(vRoot)) {
			if (!p.folder && !p.getFileName().startsWith(".")) {
				// ignoring features / plugin subdirectory, only compare
				// descriptor files
				changed = compareWithWeb(p,versionSubfolder);
				if (changed) {
					break;
				}
			}
		}
		if (changed) {
			LOG.info("New version available on net, update will be performed.");
		}
		return changed;
	}

	private boolean compareWithWeb(Path cachePath,String version) {
		try {
			String wp = getRealHttpPath(cachePath,version);
			File tmp = repoCache.createTmpFile(cachePath);
			LOG.debug("Comparing " + cachePath + " with web " + wp);
			QueryResponse response = repoCache.client.get(new HttpGet(tmp, wp,
					repoCache.getConfiguration()));
			QueryResponse cached = repoCache.getCache(cachePath);
			if (!cached.equals(response)) {
				LOG.debug("Change detected, new version will be created.");
				return true;
			} else {
				LOG.debug("No change detected");
				return false;
			}
		} catch (Exception e) {
			LOG.error("Cannot compare resource with web. Assuming resource is "
					+ "same as defined in cache " + cachePath);
		}
		return false;
	}

	private String getRealHttpPath(Path cachePath,String version) {
		Path webPath = new Path(cachePath);
		webPath.remove(webPath.pieces.lastIndexOf(version)).remove(0);
		String wp = webPath.pieces.get(0) + "://"; // protocol
		webPath.remove(0);
		wp += webPath.toStringPath();
		return wp;
	}

	private Integer getLatestVersionInCache() {
		Integer latestFolder = null;
		for (int i = 1; i < MAX_VERSIONS; i++) {
			Path p = new Path(pd.path + "/" + getVersionSubfolder(i));
			if (repoCache.exists(p)) {
				latestFolder = i;
			} else {
				break;
			}
		}
		return latestFolder;
	}

	private Path getRelativePath(Path container, Path content) {
		Path p = new Path(content);
		if (content.pieces.size() >= container.pieces.size()) {
			p.pieces = p.pieces.subList(container.pieces.size(), content.pieces.size());
			return p;
		}
		return null;
	}

	public static String getVersionSubfolder(int version) {
		return "v" + version;
	}
}
