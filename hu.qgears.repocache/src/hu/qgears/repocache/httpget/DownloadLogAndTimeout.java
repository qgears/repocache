package hu.qgears.repocache.httpget;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.commons.UtilTimer;

public class DownloadLogAndTimeout implements AutoCloseable, Callable<Object>
{
	private Log log=LogFactory.getLog(DownloadLogAndTimeout.class);
	private String url="none";
	private long t0=System.nanoTime();
	private long t1=t0;
	private long sum;
	private long l;
	private boolean closed=false;
	
	/**
	 * After 5 secs of receiving no data, a stall warning is emitted.
	 */
	public static final long STALL_WARNING_TIME_MS = 5000;
	
	/**
	 * Minimum download speed is about 100 KBps 
	 */
	private static final long MIN_BPS = 100000;
	
	public DownloadLogAndTimeout() {
		UtilTimer.getInstance().executeTimeout(STALL_WARNING_TIME_MS, this);
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
		final boolean stallWarning;
		final long bps;
		final long remaining;
		
		synchronized (this) {
			if(closed)
			{
				return null;
			}
			long tNs = System.nanoTime();
			long dtMs = TimeUnit.NANOSECONDS.toMillis(tNs - t1);
			
			if(dtMs==0l)
			{
				// Avoid div by zero
				dtMs=1;
			}
			bps=(sum*1000/dtMs);
			remaining=l-sum;
			float eta=((float)remaining/bps);
			
			log.debug("Download progress: " + url + " " + sum + "/" + l + " " 
			+ bps + "BPS ETA: " + eta + " seconds");
			
			stallWarning = (dtMs > STALL_WARNING_TIME_MS && sum == 0l) 
					|| (bps < MIN_BPS && dtMs > STALL_WARNING_TIME_MS);
		}
		
		if (stallWarning) {
			log.error("Download stalled - abort: " + url + " " + sum + "/" + l 
					+ " " + bps + " BPS; ETA: " + ((float) remaining / bps) 
					+ " seconds");
		}

		if (!closed) {
			UtilTimer.getInstance().executeTimeout(STALL_WARNING_TIME_MS, this);
		}

		return null;
	}
}
