package hu.qgears.repocache.log;

import hu.qgears.repocache.ClientQuery;

public class AccessLog {
	public final LogEventList didNotChange=new LogEventList();
	public final LogEventList updated=new LogEventList();
	public final LogEventList localOnly=new LogEventList();
	public final LogEventList errorDownload=new LogEventList();
	public final LogEventList fromCache=new LogEventList();
	public final LogEventList missingCache=new LogEventList();
	
	
	public void pathDidNotChange(ClientQuery q) {
		add(didNotChange, path(q));
	}


	public void pathUpdated(ClientQuery q) {
		add(updated, path(q));
	}

	public void localOnly(ClientQuery q) {
		add(localOnly, path(q));
	}

	private String path(ClientQuery q) {
		String s=q.getPathString();
		String orig=q.getOriginalPath().toStringPath();
		if(!orig.equals(s))
		{
			return s+" (rewrite from: "+orig+")";
		}
		return s;
	}


	public void errorDownloading(ClientQuery q) {
		add(errorDownload, path(q));
	}

	public void fromCache(ClientQuery q) {
		add(fromCache, path(q));
	}

	public void missingCache(ClientQuery q) {
		add(missingCache, path(q));
	}


	private void add(LogEventList a, String msg) {
		a.add(msg);
	}


	/**
	 * Clears all messages from the accesslog. 
	 */
	public void clear() {
		didNotChange.clear();
		updated.clear();
		localOnly.clear();
		errorDownload.clear();
		fromCache.clear();
        missingCache.clear();
	}
}
