package hu.qgears.repocache.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Minimal server for testing streaming download.
 * Can reply download requests with a "big" file and "limited" download speed.
 */
public class TestServer extends AbstractHandler {
	private Server server;
	private CountDownLatch startupSync = new CountDownLatch(1);

	public static final int PAYLOAD_SIZE_BYTES = 5000000;

	class HandleInstance
	{

		int bytesPerSec=200000;
		long n=0;
		long t0=System.currentTimeMillis();

		public void handle(Request baseRequest, HttpServletResponse response) throws IOException {
			System.out.println("Estimated download secs: "+PAYLOAD_SIZE_BYTES/bytesPerSec);
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentLengthLong(PAYLOAD_SIZE_BYTES);
			baseRequest.setHandled(true);
			try(OutputStream os=response.getOutputStream())
			{
				try(Writer wr=new OutputStreamWriter(os))
				{
					try {
						int i=0;
						while(n<PAYLOAD_SIZE_BYTES)
						{
							while(getMeasuredBps()>bytesPerSec)
							{
								System.out.println("n: "+n+" "+getMeasuredBps());
								wr.flush();
								Thread.sleep(100);
//								System.out.println("n: "+n+" dt: "+dt+" "+measuredBps);
							}
							String s="Hello "+i+"<br/>\n";
							wr.write(s);
							n+=s.length();
							i++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		private long getMeasuredBps() {
			long t=System.currentTimeMillis()+1;
			long dt=t-t0;
			long measuredBps=n*1000/(dt);
			return measuredBps;
		}
		
	}
	
	public void startHttpServer() throws Exception
	{
		server = new Server(8081);
		server.setHandler(this);
		server.start();
		startupSync.countDown();
		server.join();
	}
	
	public void waitForStartup() throws InterruptedException {
		startupSync.await(10, TimeUnit.SECONDS);
	}
	
	public void stopHttpServer() throws Exception {
		server.stop();
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String pi=baseRequest.getPathInfo();
		
		if(pi.length()<1||pi.endsWith("/")) {
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().write("<a href='a.txt'>a.txt</a>");
		} else {
			new HandleInstance().handle(baseRequest, response);
		}
	}

	public static void main(String[] args) throws Exception {
		new TestServer().doStart();
	}
}
