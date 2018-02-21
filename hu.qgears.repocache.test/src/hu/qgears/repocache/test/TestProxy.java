package hu.qgears.repocache.test;

import org.eclipse.jetty.server.Server;

/**
 * Manual launching of proxy server.
 */
public class TestProxy {
	public static void main(String[] args) throws Exception {
		ProxyHandler rh = new ProxyHandler();

		Server server = new Server(9000);
		server.setHandler(rh);
		server.start();
		server.join();
	}
}
