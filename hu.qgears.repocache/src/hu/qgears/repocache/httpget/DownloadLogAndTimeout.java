package hu.qgears.repocache.httpget;

import java.util.concurrent.Callable;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.commons.UtilTimer;

public class DownloadLogAndTimeout implements AutoCloseable, Callable<Object>
{
	private Log log=LogFactory.getLog(DownloadLogAndTimeout.class);
	private GetMethod method;
	private String url="none";
	private long t0=System.nanoTime();
	private long t1=t0;
	private long sum;
	private long l;
	private boolean closed=false;
	/**
	 * After 30 secs without reply query is aborted.
	 */
	private long timeoutReply=30000;
	/**
	 * Minimum download speed is about 100 KBps 
	 */
	private long minBps=100000;
	public DownloadLogAndTimeout(GetMethod method) {
		this.method=method;
		try {
			url=method.getURI().toString();
		} catch (Exception e) {
			log.error("Error decoding URI of download query", e);
		}
		UtilTimer.getInstance().executeTimeout(1000, this);
	}

	@Override
	public void close() {
		closed=true;
	}

	public void progress(long sum, long l) {
		synchronized (this) {
			if(this.sum==0l)
			{
				t1=System.nanoTime();
			}
			this.sum=sum;
			this.l=l;
		}
	}

	@Override
	public Object call() throws Exception {
		boolean reinit;
		boolean close;
		long bps;
		long remaining;
		synchronized (this) {
			if(closed)
			{
				return null;
			}
			long t=System.nanoTime();
			long dt=(t-t1)/1000000l;
			if(dt==0l)
			{
				// Avoid div by zero
				dt=1;
			}
			bps=(sum*1000/dt);
			remaining=l-sum;
			float eta=((float)remaining/bps);
			log.info("Download progress: "+url+" "+sum+"/"+l+" "+bps+"BPS ETA: "+eta+" seconds");
			reinit=!closed;
			close=(dt>timeoutReply&&sum==0l)||(bps<minBps&&dt>timeoutReply);
		}
		if(close)
		{
			log.error("Download stalled - abort: "+url+" "+sum+"/"+l+" "+bps+"BPS"+" ETA: "+((float)remaining/bps)+" seconds");
			method.abort();
		}
		if(reinit)
		{
			UtilTimer.getInstance().executeTimeout(1000, this);
		}
		return null;
	}
}
