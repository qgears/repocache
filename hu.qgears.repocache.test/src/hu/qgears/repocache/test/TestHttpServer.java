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
		setHandler(new SoTimeoutHandler());
	}
	
	private TestHttpServer(final int port) {
		super(new InetSocketAddress(port));
		setHandler(new SoTimeoutHandler());
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
	
	/**
	 * Simple command line interface, which runs until the process is terminated.
	 * For example, Ctrl+C is an appropriate way to exit from the server.
	 * @param args one single integer, which is the port - if not specified, 
	 * port is defaulted to 20000
	 * @throws Exception passed up if an error is encountered during starting up
	 * the server
	 */
	public static void main(final String... args) throws Exception {
		final int port = args.length > 0 ? Integer.parseInt(args[0]) : 20000;
		final TestHttpServer instance = new TestHttpServer(port);
		instance.start();
	}
}
