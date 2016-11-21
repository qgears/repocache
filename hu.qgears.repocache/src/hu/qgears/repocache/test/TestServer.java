package hu.qgears.repocache.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

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
	class HandleInstance
	{

		int bytesPerSec=10000000;
		long n=0;
		long t0=System.currentTimeMillis();
		int N=500000000;

		public void handle(Request baseRequest, HttpServletResponse response) throws IOException {
			System.out.println("Estimated download secs: "+N/bytesPerSec);
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentLengthLong(N);
			baseRequest.setHandled(true);
			try(OutputStream os=response.getOutputStream())
			{
				try(Writer wr=new OutputStreamWriter(os))
				{
					try {
						int i=0;
						while(n<N)
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
						// TODO Auto-generated catch block
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
	public static void main(String[] args) throws Exception {
		new TestServer().starts();
	}
	private void starts() throws Exception
	{
		Server server = new Server(8080);
		server.setHandler(this);
		server.start();
		server.join();
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		new HandleInstance().handle(baseRequest, response);
	}
}
