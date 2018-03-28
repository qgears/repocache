package hu.qgears.repocache.httpget;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.config.RepoConfiguration;

public class StreamingHttpClient {
	private static final Log logger=LogFactory.getLog(StreamingHttpClient.class);
	public static final int bufferSize=1024*1024;
	
	/**
	 * Creates a configured HTTP client. 
	 * @param get the HTTP get request, for fulfilling which, a client will be 
	 * created
	 * @return the HTTP client that will handle the get request
	 */
	private HttpClient createHttpClient(final HttpGet get) {
		final RepoConfiguration repocacheConfig = get.getRepoConfiguration();
		// Create an instance of HttpClient
		// XXX a HttpClient can and is advised to be reused according to this:
		// http://hc.apache.org/httpclient-3.x/performance.html#Reuse_of_HttpClient_instance
		final HttpClient client = new HttpClient();
		
		if (repocacheConfig.isUpstreamHttpProxyConfigured()) {
			final ProxyHost proxyHost = new ProxyHost(
					repocacheConfig.getUpstreamHttpProxyHostname(), 
					repocacheConfig.getUpstreamHttpProxyPort());
			client.getHostConfiguration().setProxyHost(proxyHost);
		}
		
		final HttpConnectionManagerParams httpConnParams = 
				client.getHttpConnectionManager().getParams();
		
		httpConnParams.setConnectionTimeout(repocacheConfig.getHttpConnectionTimeoutMs());
		httpConnParams.setSoTimeout(repocacheConfig.getHttpSoTimeoutMs());
		
		return client;
	}
	
	/**
	 * 
	 * @param get
	 * @return never null
	 * @throws HttpException
	 * @throws IOException
	 */
	public QueryResponse get(HttpGet get) throws HttpException, IOException {
		final HttpClient client = createHttpClient(get);
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
		} catch (final SSLHandshakeException she) {
			throw she;
		} finally {
			get.close();
			method.abort();
			// Release the connection.
			method.releaseConnection();
		}
	}
}
