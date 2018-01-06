package hu.qgears.repocache.https;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import hu.qgears.repocache.ssh.SSLDynamicCert;

/**
 * Man In The Middle implementation for https
 * For the required server name generates a certificate and opens a single instance of that server.
 */
public class DynamicSSLProxyConnector implements IConnector
{
	private SSLDynamicCert sslDc;
	private IDecodedClientHandler decodedClientHandler;
	public DynamicSSLProxyConnector(SSLDynamicCert sslDc, IDecodedClientHandler decodedClientHandler) {
		this.decodedClientHandler=decodedClientHandler;
		this.sslDc=sslDc;
	}

	@Override
	public IConnection connect(final String targethost, final int targetport) throws NoConnectException {
		try {
			SSLServerSocketFactory fact=sslDc.openServer(targethost);
			final SSLServerSocket ss=(SSLServerSocket)fact.createServerSocket();
			ss.bind(new InetSocketAddress("127.0.0.1", 0));
			int port=ss.getLocalPort();
			new Thread("SSH Server accept thread "+targethost+":"+targetport){
				public void run() {
					try {
						Socket client;
						try
						{
							client=ss.accept();
						}
						finally
						{
							try {
								ss.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						decodedClientHandler.handleDecodedClient(client, targethost, targetport);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
			}.start();
			try
			{
				Socket s=new Socket("localhost", port);
				return new Connection("SSL MITM "+targethost+":"+targetport, s, s.getOutputStream(), s.getInputStream(), false);
			}catch(Exception e)
			{
				throw new NoConnectException("Problem");
			}
		} catch (Exception e) {
			throw new NoConnectException(e);
		}
	}

	protected void handleDecodedClient(Socket client, String targethost, int targetport) {
	}

}
