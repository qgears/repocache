package hu.qgears.repocache.httpget;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.TimeoutController.TimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.config.RepoConfiguration;

public class StreamingHttpClient {
	public static final int bufferSize=1024*1024;
	final static Log logger=LogFactory.getLog(StreamingHttpClient.class);
	/**
	 * 
	 * @param get
	 * @return never null
	 * @throws HttpException
	 * @throws IOException
	 */
	public QueryResponse get(HttpGet get) throws HttpException, IOException {
		// Create an instance of HttpClient
		HttpClient client = new HttpClient();
		
		final HttpConnectionManagerParams httpConnParams = 
				client.getHttpConnectionManager().getParams();
		final RepoConfiguration repocacheConfig = get.getRepoConfiguration();
		
		httpConnParams.setConnectionTimeout(repocacheConfig.getHttpConnectionTimeoutMs());
		
		httpConnParams.setSoTimeout(repocacheConfig.getHttpSoTimeoutMs());
		
		// Create a method instance.
		GetMethod method = new GetMethod(get.url);
		
		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
				new DefaultHttpMethodRetryHandler(3, false));

		try(DownloadLogAndTimeout log=new DownloadLogAndTimeout()) {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new FileNotFoundException("Method failed: " 
			+ method.getStatusLine()+" "+get.url);
			}
			long l=-1;
			Header h=method.getResponseHeader("Content-Length");
			if(h!=null)
			{
				try {
					l=Long.parseLong(h.getValue());
				} catch (Exception e) {
					logger.error("Error parsing response header length, value: " 
				+ h.getValue(), e);
				}
			}
			long sum=0;
			try(OutputStream fos=get.createOutputStream(l))
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
			QueryResponse ret=get.ready(method);
			if(ret.folder)
			{
				UrlFixer.fixUrls(ret);
			}
			h=method.getResponseHeader("Content-Type");
			if(h!=null)
			{
				ret.contentType=h.getValue();
			}
			return ret;
		} catch (final ConnectTimeoutException | SocketTimeoutException te) {
			throw te;
		} finally {
			get.close();
			method.abort();
			// Release the connection.
			method.releaseConnection();
		}
	}
}
