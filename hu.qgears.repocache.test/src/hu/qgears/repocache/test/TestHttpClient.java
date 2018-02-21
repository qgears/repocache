package hu.qgears.repocache.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import hu.qgears.commons.UtilFile;

/**
 * Test HTTP client which utilizes a proxy
 *  
 * @author chreex
 */
public class TestHttpClient {
	private TestHttpClient() {};
	/**
	 * Downloads a resource through a proxy
	 * @param urlString the resource locator, by which the resource will be
	 * downloaded
	 * @param proxyPort the port of the proxy server. Note that the proxy host 
	 * is {@code localhost}. 
	 * @throws MalformedURLException passed up from 
	 * {@link URL#openConnection(Proxy)}
	 * @throws IOException passed up from 
	 * {@link URL#openConnection(Proxy)}
	 */
	public static byte[] download(final String urlString, final int proxyPort) 
			throws MalformedURLException, IOException {
		final Proxy proxy = new Proxy(Proxy.Type.HTTP, 
				new InetSocketAddress("localhost", proxyPort));
		final URLConnection openConnection = new URL(urlString).openConnection(proxy);
		final byte[] data = UtilFile.loadFile(openConnection.getInputStream());
		
		return data;
	}
}
