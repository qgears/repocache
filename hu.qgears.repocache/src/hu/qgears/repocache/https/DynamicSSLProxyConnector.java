package hu.qgears.repocache.https;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Function;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import hu.qgears.repocache.CommandLineArgs;

/**
 * Man In The Middle implementation for https
 * For the required server name generates a certificate and opens a single instance of that server.
 */
public class DynamicSSLProxyConnector implements IConnector
{
	private String connectHost;
	private Function<Object, Integer> connectPort;
	private CommandLineArgs args;
	public DynamicSSLProxyConnector(CommandLineArgs args, String connectHost, Function<Object, Integer> connectPort) {
		this.args=args;
		this.connectHost=connectHost;
		this.connectPort=connectPort;
	}

	@Override
	public IConnection connect(final String targethost, final int targetport) throws NoConnectException {
		try {
			SSLServerSocketFactory fact=args.getDynamicCertSupplier().openServer(targethost);
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
						handleDecodedClient(client, targethost, targetport);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
			}.start();
			try
			{
				Socket s=new Socket("localhost", port);
				return new Connection("SSL MITM "+targethost+":"+targetport, s, false);
			}catch(Exception e)
			{
				throw new NoConnectException("Problem");
			}
		} catch (Exception e) {
			throw new NoConnectException(e);
		}
	}

	protected void handleDecodedClient(Socket client, String targethost, int targetport) {
		try {
			Socket proxy=new Socket(connectHost, connectPort.apply(this));
			System.err.println("Connected to: "+connectHost+":"+connectPort.apply(this));
			try(Connection c=new Connection("SSL plaintext "+targethost+":"+targetport, proxy, false))
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
