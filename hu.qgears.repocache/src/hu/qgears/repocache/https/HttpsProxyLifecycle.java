package hu.qgears.repocache.https;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.component.AbstractLifeCycle;

public class HttpsProxyLifecycle extends AbstractLifeCycle {
	private String serverHost;
	private int port;
	private DynamicSSLProxyConnector dsslProxyConnector;
	public HttpsProxyLifecycle(String serverHost, int port,
			DynamicSSLProxyConnector dsslProxyConnector) {
		super();
		this.serverHost = serverHost;
		this.port = port;
		this.dsslProxyConnector = dsslProxyConnector;
	}
	private HttpsProxyServer s;
	@Override
	protected void doStart() throws Exception {
		s=new HttpsProxyServer(serverHost, port, dsslProxyConnector);
		s.start();
		s.started.get();
	}
	@Override
	protected void doStop() throws Exception {
		s.stopServer();
		s.stopped.get(getStopTimeout(), TimeUnit.MILLISECONDS);
	}
}
