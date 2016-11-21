package hu.qgears.repocache.httpget;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.repocache.QueryResponse;

public class StreamingHttpClient {
	final static Logger log=LoggerFactory.getLogger(StreamingHttpClient.class);
	/**
	 * 
	 * @param get
	 * @return never null
	 * @throws HttpException
	 * @throws IOException
	 */
	public QueryResponse get(HttpGet get) throws HttpException, IOException {
		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();
		// Create a method instance.
		GetMethod method = new GetMethod("http://localhost:8080/a.txt");
//		GetMethod method = new GetMethod("http://releases.ubuntu.com/16.04.1/ubuntu-16.04.1-desktop-amd64.iso");
		

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try(DownloadLogAndTimeout log=new DownloadLogAndTimeout(method)) {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new FileNotFoundException("Method failed: " + method.getStatusLine());
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
			long sum=0;
			try(OutputStream fos=get.createOutputStream(l))
			{
				try(InputStream is=method.getResponseBodyAsStream())
				{
					byte[] buffer=new byte[1000000];
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
			get.ready();
			return new QueryResponse(method, get);
		} finally {
			get.close();
			method.abort();
			// Release the connection.
			method.releaseConnection();
		}
	}
}