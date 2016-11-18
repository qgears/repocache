package hu.qgears.repocache.httpget;

import java.util.concurrent.Callable;

import hu.qgears.commons.UtilTimer;

public class HttpClientLogProgress implements Callable<Object>{
	private String url;
	private long t0=System.currentTimeMillis();
	public HttpClientLogProgress(String url) {
		this.url=url;
		UtilTimer.getInstance().executeTimeout(10000, this);
	}
	private volatile boolean cancelled;

	public void cancel() {
		cancelled=true;
	}

	@Override
	public Object call() throws Exception {
		if(!cancelled)
		{
			long diff=System.currentTimeMillis()-t0;
			HttpClientToInternet.log.info("Downloading: "+url+" progress: "+(diff/1000)+" seconds.");
			UtilTimer.getInstance().executeTimeout(10000, this);
		}
		return null;
	}

}
