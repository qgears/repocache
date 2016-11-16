package hu.qgears.repocache;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientToInternet {
	final static Logger log=LoggerFactory.getLogger(HttpClientToInternet.class);
	public static void main(String[] args) throws HttpException, IOException {
		QueryResponse r=new HttpClientToInternet().get("http://qgears.com/opensource/updates");
		System.out.println(r);
		// Deal with the response.
		// Use caution: ensure correct character encoding and is not binary
		// data
		System.out.println(new String(r.responseBody));

	}

	/**
	 * 
	 * @param url
	 * @return never null
	 * @throws HttpException
	 * @throws IOException
	 */
	public QueryResponse get(String url) throws HttpException, IOException {
		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();

		// Create a method instance.
		GetMethod method = new GetMethod(url);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		HttpClientLogProgress progress=new HttpClientLogProgress(url);
		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new FileNotFoundException("Method failed: " + method.getStatusLine());
			}
			return new QueryResponse(method);
		} finally {
			progress.cancel();
			// Release the connection.
			method.releaseConnection();
		}
	}
}