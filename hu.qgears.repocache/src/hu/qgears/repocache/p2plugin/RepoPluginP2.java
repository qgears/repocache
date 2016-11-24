package hu.qgears.repocache.p2plugin;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.repocache.AbstractRepoPlugin;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientQueryInternal;
import hu.qgears.repocache.Path;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.RepoCache;
import hu.qgears.repocache.RepoHandler;
import hu.qgears.repocache.TimestampParser;
import hu.qgears.repocache.httpget.HttpGet;

public class RepoPluginP2 extends AbstractRepoPlugin
{
	private Logger log=LoggerFactory.getLogger(RepoPluginP2.class);
	private RepoCache rc;
	private RepoHandler rh;

	public RepoPluginP2(RepoCache rc, RepoHandler rh) {
		this.rc=rc;
		this.rh = rh;
	}

	public String getPath() {
		return P2VersionFolderUtil.P2_PATH;
	}
	public Map<String, P2RepoConfig> getP2Repos() {
		return new TreeMap<>(rc.getConfiguration().getP2repos());
	}
	
	private P2RepoConfig getRepoConfig(String repoName) {
		if (repoName == null) return null;
		for (Map.Entry<String, P2RepoConfig> entry : rc.getConfiguration().getP2repos().entrySet()) {
			if (repoName.equals(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	private boolean isUpdateModeNormal(String repoName) {
		P2RepoConfig conf = getRepoConfig(repoName);
		return (conf==null || P2RepoMode.normal.equals(conf.getRepoMode()));
	}
	
	private boolean createNewVersionOnRewriteMode(Path path) {
		int lastVersion = P2VersionFolderUtil.getInstance().getLastVersionUsed(path.pieces.get(1));
		Path placeholderFile=new Path(path).add(""+(++lastVersion)).add(".folder");
		try {
			log.info("Update is forbidden, create for repo " + path.pieces.get(1) + ", version: " + lastVersion);
			rc.createFile(placeholderFile, new byte[]{}, "Auto update create new P2 repo version: ");
		} catch (IOException e) {
			log.error("Error ", e);

		}
		return true;
	}
	
	@Override
	public QueryResponse getOnlineResponse(Path localPath, ClientQuery q, QueryResponse cachedContent, boolean netAllowed) throws IOException {
		if(localPath.pieces.size()==0)
		{
			return new P2Listing(q, this).generate();
		}
		if(!localPath.folder&&localPath.pieces.size()==1 &&localPath.pieces.get(0).equals(P2CompositeArtifacts.file))
		{
			long timestamp=parseTimeStamp(cachedContent);
			QueryResponse ret=new P2CompositeArtifacts(q, this, timestamp).generate();
			if(!ret.equals(cachedContent))
			{
				// In case the listing has changed also update the timestamp
				ret=new P2CompositeArtifacts(q, this, System.currentTimeMillis()).generate();
			}
			return ret;
		}else if(!localPath.folder&&localPath.pieces.size()==1 &&localPath.pieces.get(0).equals(P2CompositeContent.file))
		{
			long timestamp=parseTimeStamp(cachedContent);
			QueryResponse ret=new P2CompositeContent(q, this, timestamp).generate();
			if(!ret.equals(cachedContent))
			{
				// In case the listing has changed also update the timestamp
				ret=new P2CompositeContent(q, this, System.currentTimeMillis()).generate();
			}
			return ret;
		}
		P2RepoConfig config = getRepoConfig(localPath.pieces.get(0));
		if (config != null) {
			if (localPath.pieces.size()==1) {
				Path p = P2VersionFolderUtil.getInstance().createP2VersionFolderIfNotExist(localPath.pieces.get(0));
				if (p!=null){
					createNewVersionOnRewriteMode(p);
				}
				return new P2RepoVersionListing(q, this, localPath.pieces.get(0)).generate();
			}
			if(!localPath.folder&&localPath.pieces.size()==2 &&localPath.pieces.get(1).equals(P2RepoVersionArtifacts.file))
			{
				Path relpath = P2VersionFolderUtil.getInstance().getLastVersionPath(localPath.pieces.get(0));
				updateLatestListings(q, relpath);
				long timestamp=parseTimeStamp(cachedContent);
				QueryResponse ret=new P2RepoVersionArtifacts(q, this, timestamp, localPath.pieces.get(0)).generate();
				if(!ret.equals(cachedContent))
				{
					// In case the listing has changed also update the timestamp
					ret=new P2RepoVersionArtifacts(q, this, System.currentTimeMillis(), localPath.pieces.get(0)).generate();
				}
				return ret;
			} else if(!localPath.folder&&localPath.pieces.size()==2 &&localPath.pieces.get(1).equals(P2RepoVersionContent.file)) {
				Path relpath = P2VersionFolderUtil.getInstance().getLastVersionPath(localPath.pieces.get(0));
				updateLatestListings(q, relpath);
				long timestamp=parseTimeStamp(cachedContent);
				QueryResponse ret=new P2RepoVersionContent(q, this, timestamp, localPath.pieces.get(0)).generate();
				if(!ret.equals(cachedContent))
				{
					// In case the listing has changed also update the timestamp
					ret=new P2RepoVersionContent(q, this, System.currentTimeMillis(), localPath.pieces.get(0)).generate();
				}
				return ret;
			}
			Path ref = new Path(localPath).remove(0);
			ref.remove(0);
			String httpPath = config.getBaseUrl() + ref.toStringPath();
			if(netAllowed)
			{
				try
				{
					QueryResponse response = q.rc.client.get(new HttpGet(q.rc.createTmpFile(q.path), httpPath));
					if(response!=null&&!(response.equals(cachedContent)))
					{
						if (cachedContent != null && !isUpdateModeNormal(localPath.pieces.get(0))) {
							String currentVersion = localPath.pieces.get(1);
							Path lastVersion = P2VersionFolderUtil.getInstance().getLastVersionPath(localPath.pieces.get(0));
							if (lastVersion.getFileName().equals(currentVersion)) {
								createNewVersionOnRewriteMode(lastVersion.removeLast());
								return null;
							} else {
								log.info("Local cache not equal no remote, but P2 repo " + localPath.pieces.get(0) + "version (not last) is readonly.");
								return null;
							}
						}
					}
					return response;
				}catch(FileNotFoundException e)
				{
					if(ref.pieces.size()==2 && ref.pieces.get(1).equals("p2.index"))
					{
						// Workaround missing p2.index file in composited repo
						return new P2Index(q).generate();
					}
					throw e;
				}
			}else
			{
				log.info("File in cache is not updated: "+q.path);
			}
		}
		return null;
	}

	private void updateLatestListings(ClientQuery q, Path relpath) {
		// Update compositeArtifact
		updatePath(q, new Path(relpath, "compositeArtifacts.xml"));
		// Update compositeContent
		updatePath(q, new Path(relpath, "compositeContent.xml"));
	}

	private void updatePath (ClientQuery q, Path relpath) {
		ClientQueryInternal subq=new ClientQueryInternal(rc, relpath, q);
		try {
			rh.getQueryResponse(subq);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private long parseTimeStamp(QueryResponse cachedContent) {
		TimestampParser tp=new TimestampParser();
		if(cachedContent!=null&&!cachedContent.folder)
		{
			try {
				SAXParser p=SAXParserFactory.newInstance().newSAXParser();
				p.parse(new ByteArrayInputStream(cachedContent.getResponseAsBytes()), tp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tp.getTimestamp();
	}
	
}
