package hu.qgears.repocache.https;

import java.io.IOException;
import java.net.Socket;

public class DecodedClientHandlerToProxy implements IDecodedClientHandler {

	private String connectHost;
	private int connectPort;
	private String rewriteInfo;
	public DecodedClientHandlerToProxy(String connectHost, int connectPort, String rewriteInfo) {
		super();
		this.connectHost = connectHost;
		this.connectPort = connectPort;
		this.rewriteInfo=rewriteInfo;
	}
	@Override
	public void handleDecodedClient(Socket client, String targethost,
			int targetport) {
		try {
			Socket proxy=new Socket(connectHost, connectPort);
			RewriteOutputStreamFilter rewrite=new RewriteOutputStreamFilter(proxy.getOutputStream(), targethost, targetport, rewriteInfo);
			try(Connection c=new Connection("SSL plaintext "+targethost+":"+targetport, proxy, rewrite, proxy.getInputStream(), false))
			{
				c.connectStreams(client, client.getInputStream(), client.getOutputStream());
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
