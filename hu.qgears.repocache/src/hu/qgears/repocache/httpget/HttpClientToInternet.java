package hu.qgears.repocache.httpget;

import java.io.File;
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

import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.QueryResponseByteArray;

public class HttpClientToInternet {
	final static Logger log=LoggerFactory.getLogger(HttpClientToInternet.class);
	public static void main(String[] args) throws HttpException, IOException {
		QueryResponse r=new HttpClientToInternet().get(new HttpGet(new File("/tmp/linkrewrite.txt"), "http://qgears.com/opensource/updates"));
		System.out.println(r);
		// Deal with the response.
		// Use caution: ensure correct character encoding and is not binary
		// data
		System.out.println(r.getResponseAsString());

	}

	/**
	 * 
	 * @param url
	 * @return never null
	 * @throws HttpException
	 * @throws IOException
	 */
	public QueryResponse get(HttpGet get) throws HttpException, IOException {
		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();

		// Create a method instance.
		GetMethod method = new GetMethod(get.url);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		HttpClientLogProgress progress=new HttpClientLogProgress(get.url);
		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new FileNotFoundException("Method failed: " + method.getStatusLine()+" '"+get.url+"'");
			}
			return new QueryResponseByteArray(method.getURI().toString(), method.getResponseBody());
		} finally {
			progress.cancel();
			// Release the connection.
			method.releaseConnection();
		}
	}
}