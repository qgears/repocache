package hu.qgears.repocache;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepoPluginP2 extends AbstractRepoPlugin
{
	private Logger log=LoggerFactory.getLogger(RepoPluginP2.class);
	private Map<String, RepoConfig> p2repos = new HashMap<>();
	public RepoPluginP2() {
		// TODO parameter aliases
		p2repos.put("qgears-opensource", new RepoConfig("opensource/updates", "http://qgears.com/", "http://qgears.com/"));
		p2repos.put("eclipse-neon", new RepoConfig("technology/epp/packages/neon", "http://download.eclipse.org/", "http://mirror.tspu.ru/eclipse/"));
		p2repos.put("eclipse-release-neon-201610111000", new RepoConfig("releases/neon/201610111000", "http://download.eclipse.org/", "http://mirror.tspu.ru/eclipse/"));
		p2repos.put("oomph-updates-milestone-latest", new RepoConfig("oomph/updates/milestone/latest", "http://download.eclipse.org/", "http://mirror.tspu.ru/eclipse/"));
	}

	public String getPath() {
		return "p2";
	}
	public Map<String, RepoConfig> getP2Repos() {
		return new TreeMap<>(p2repos);
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
		for (Map.Entry<String, RepoConfig> entry : p2repos.entrySet()) {
			if (localPath.pieces.get(0).equals(entry.getKey())) {
				Path ref = new Path(localPath).remove(0);
				String httpPath = entry.getValue().getBaseUrl() + ref.toStringPath();
				if(netAllowed)
				{
					try
					{
						QueryResponse response = q.rc.client.get(httpPath);
						return response;
					}catch(FileNotFoundException e)
					{
						if(ref.pieces.size()==1 && ref.pieces.get(0).equals("p2.index"))
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
		}
		return null;
	}

	private long parseTimeStamp(QueryResponse cachedContent) {
		TimestampParser tp=new TimestampParser();
		if(cachedContent!=null&&!cachedContent.folder)
		{
			try {
				SAXParser p=SAXParserFactory.newInstance().newSAXParser();
				p.parse(new ByteArrayInputStream(cachedContent.responseBody), tp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tp.getTimestamp();
	}

}
