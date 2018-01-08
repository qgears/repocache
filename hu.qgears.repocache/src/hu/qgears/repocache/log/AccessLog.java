package hu.qgears.repocache.log;

import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.Path;

public class AccessLog {
	public final LogEventList didNotChange=new LogEventList();
	public final LogEventList updated=new LogEventList();
	public final LogEventList localOnly=new LogEventList();
	public final LogEventList errorDownload=new LogEventList();
	public final LogEventList fromCache=new LogEventList();
	public final LogEventList missingCache=new LogEventList();
	
	
	public void pathDidNotChange(Path path) {
		add(didNotChange, path.toStringPath());
	}


	public void pathUpdated(Path path) {
		add(updated, path.toStringPath());
	}

	public void localOnly(ClientQuery q) {
		add(localOnly, q.getPath().toStringPath());
	}

	public void errorDownloading(ClientQuery q) {
		add(errorDownload, q.getPathString());
	}

	public void fromCache(ClientQuery q) {
		add(fromCache, q.getPathString());
	}

	public void missingCache(ClientQuery q) {
		add(missingCache, q.getPathString());
	}


	private void add(LogEventList a, String msg) {
		a.add(msg);
	}

}
