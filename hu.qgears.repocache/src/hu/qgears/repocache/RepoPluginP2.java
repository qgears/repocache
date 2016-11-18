package hu.qgears.repocache;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.repocache.httpget.HttpGet;

public class RepoPluginP2 extends AbstractRepoPlugin
{
	private Logger log=LoggerFactory.getLogger(RepoPluginP2.class);
	private RepoCache rc;

	public RepoPluginP2(RepoCache rc) {
		this.rc=rc;
	}

	public String getPath() {
		return "p2";
	}
	public Map<String, P2RepoConfig> getP2Repos() {
		return new TreeMap<>(rc.getConfiguration().getP2repos());
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
		for (Map.Entry<String, P2RepoConfig> entry : rc.getConfiguration().getP2repos().entrySet()) {
			if (localPath.pieces.get(0).equals(entry.getKey())) {
				Path ref = new Path(localPath).remove(0);
				String httpPath = entry.getValue().getBaseUrl() + ref.toStringPath();
				if(netAllowed)
				{
					try
					{
						QueryResponse response = q.rc.client.get(new HttpGet(httpPath));
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
