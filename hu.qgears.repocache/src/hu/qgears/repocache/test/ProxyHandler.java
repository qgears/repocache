package hu.qgears.repocache.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.repocache.httpget.DownloadLogAndTimeout;

/**
 * Minimal proxy implementation. Basic for proxying all downloads.
 */
public class ProxyHandler extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println("Scheme: "+baseRequest.getScheme());
		System.out.println("Server: "+baseRequest.getServerName());
		System.out.println("Port: "+baseRequest.getServerPort());
		System.out.println("URI: "+baseRequest.getRequestURI());
		System.out.println("URL: "+baseRequest.getRequestURL());
		System.out.println("Path: "+baseRequest.getPathInfo());
		Enumeration<String> atts=baseRequest.getAttributeNames();
		while(atts.hasMoreElements())
		{
			String att=atts.nextElement();
			System.out.println(""+att+": "+baseRequest.getAttribute(att));
		}
		Map<String,String[]> params=baseRequest.getParameterMap();
		for(String key: params.keySet())
		{
			String[] values=params.get(key);
			if(values!=null)
			{
				for(String v: values)
				{
					System.out.println("Param: "+key+": "+v);
				}
			}
		}
		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();
		String url=baseRequest.getRequestURL().toString();
		// Create a method instance.
		GetMethod method = new GetMethod(url);
		

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try(DownloadLogAndTimeout log=new DownloadLogAndTimeout(method)) {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new FileNotFoundException("Method failed: " + method.getStatusLine()+" "+url);
			}
			long l=-1;
			Header h=method.getResponseHeader("Content-Length");
			if(h!=null)
			{
				try {
					l=Long.parseLong(h.getValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Header contentType = method.getResponseHeader("Content-Type");
			response.setContentType(contentType.getValue());
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentLength((int)l);
			baseRequest.setHandled(true);
			long sum=0;
			int bufferSize=2048;
			try(OutputStream fos=response.getOutputStream())
			{
				try(InputStream is=method.getResponseBodyAsStream())
				{
					byte[] buffer=new byte[bufferSize];
					int n;
					while((n=is.read(buffer))>0)
					{
						sum+=n;
						log.progress(sum, l);
						fos.write(buffer, 0, n);
					}
				}
			}
			if(l!=-1 && sum!=l)
			{
				// Premature close of download
				throw new IOException("Premature end of file: "+sum+"/"+l);
			}
		} finally {
			method.abort();
			// Release the connection.
			method.releaseConnection();
		}
	}
}
