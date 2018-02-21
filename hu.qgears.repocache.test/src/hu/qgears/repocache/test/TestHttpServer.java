package hu.qgears.repocache.test;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 * Test-purpose HTTP server instance to serve test-resources for testing 
 * repocache. Supported features:
 * <ul>
 * <li>socket timeout simulation
 * </ul>
 * 
 * @author chreex
 */
public class TestHttpServer extends Server {
	/**
	 * Creates a sample HTTP server with a random listening port. The random
	 * listening port can be queried after initialization by calling
	 * {@link #getPort()}.
	 */
	public TestHttpServer() {
		super(new InetSocketAddress(0));
	}
	
	/**
	 * @return the host name of the server
	 */
	public String getHost() {
		return ((ServerConnector)getConnectors()[0]).getHost();
	}

	/**
	 * @return the random port assigned to the server
	 */
	public int getPort() {
		return ((ServerConnector)getConnectors()[0]).getLocalPort();
	}
}
