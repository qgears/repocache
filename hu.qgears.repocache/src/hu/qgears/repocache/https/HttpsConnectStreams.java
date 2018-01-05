package hu.qgears.repocache.https;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.log4j.Logger;

import hu.qgears.commons.UtilFile;

/**
 * Connect two streams to each other by a thread that reads input and writes to 
 * output at once.
 * @author rizsi
 *
 */
public class HttpsConnectStreams {
	private static final Logger LOG = Logger.getLogger(HttpsConnectStreams.class);
	private String name;
	private InputStream is;
	private OutputStream os;
	private OutputStream os2;
	private Runnable afterCallback;
	public HttpsConnectStreams(final String name, final InputStream is, final OutputStream os) {
		super();
		this.name=name;
		this.is=is;
		this.os=os;
	}
	public HttpsConnectStreams setOs2(OutputStream os2) {
		this.os2 = os2;
		return this;
	}
	/**
	 * Copy all data from input stream to the output stream using this thread.
	 * @param source
	 * @param target
	 * @param closeOutput target is closed after input was consumed if true
	 * @param bufferSize size of the buffer used when copying
	 * @throws IOException
	 */
	public void doStream(final InputStream source,
			final OutputStream target, boolean closeSource, boolean closeOutput, int bufferSize) throws IOException {
		try
		{
			try {
				int n;
				byte[] cbuf = new byte[bufferSize];
				while ((n = source.read(cbuf)) > -1) {
					target.write(cbuf, 0, n);
					target.flush();
					if(os2!=null)
					{
						os2.write(cbuf, 0, n);
						os2.flush();
					}
				}
			} finally {
				if(closeSource&&source!=null)
				{
					source.close();
				}
			}
		}finally
		{
			if(closeOutput&&target!=null)
			{
				target.close();
			}
		}
	}
	private Thread t;
	public HttpsConnectStreams start()
	{
		t=new Thread(name)
		{
			public void run() {
				HttpsConnectStreams.this.run();
			};
		};
		t.start();
		return this;
	}
	private void run() {
		try {
			doStream(is, os, false, false, UtilFile.defaultBufferSize.get());
		} catch (SocketException e)
		{
			// Socket exceptions are not logged. Socked closed is normal
		} catch (Exception e) {
			LOG.error("Exception during streaming error stream: "+name, e);
		}
		if(afterCallback!=null)
		{
			afterCallback.run();
		}
		afterStreamClosed();
	}
	protected void afterStreamClosed() {
	}
	public void join() throws InterruptedException {
		t.join();
	}
	public HttpsConnectStreams setAfterCallback(Runnable connection) {
		this.afterCallback=connection;
		return this;
	}
}
