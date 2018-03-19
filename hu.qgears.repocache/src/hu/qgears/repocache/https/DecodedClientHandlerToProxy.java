package hu.qgears.repocache.https;

import java.io.IOException;
import java.net.Socket;

import hu.qgears.repocache.https.HttpsProxyConnectionsManager.RegistryEntry;

public class DecodedClientHandlerToProxy implements IDecodedClientHandler {

	private String connectHost;
	private int connectPort;
	private boolean rewriteInfo;
	public DecodedClientHandlerToProxy(String connectHost, int connectPort, boolean rewriteInfo) {
		super();
		this.connectHost = connectHost;
		this.connectPort = connectPort;
		this.rewriteInfo=rewriteInfo;
	}
	@Override
	public void handleDecodedClient(Socket client, String targethost,
			int targetport) {
		try {
			final Socket proxy=new Socket(connectHost, connectPort);
			try(Connection c=new Connection("SSL plaintext "+targethost+":"+
					targetport, proxy, proxy.getOutputStream(), 
					proxy.getInputStream(), false)) {
				try(RegistryEntry entry=HttpsProxyConnectionsManager.getInstance().register(
						proxy.getLocalPort(), targethost, targetport, rewriteInfo))
				{
					c.connectStreams(client, client.getInputStream(), client.getOutputStream());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
