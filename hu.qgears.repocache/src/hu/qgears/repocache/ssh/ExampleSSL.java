package hu.qgears.repocache.ssh;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 * Example SSL server with a specific keystore.
 */
public class ExampleSSL {
	public static void main(String[] args) throws Exception {
		System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
		System.setProperty("javax.net.ssl.keyStore", "path/to/pkcs12 file");
		System.setProperty("javax.net.ssl.keyStorePassword", "verysec");
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
		ss.bind(new InetSocketAddress("0.0.0.0", 9001));
		while (true) {
			final SSLSocket s = (SSLSocket) ss.accept();
			new Thread() {
				public void run() {
					handleSocket(s);
				};
			}.start();
		}

	}
	private static int ctr=0;
	protected static void handleSocket(SSLSocket s) {
		try {
			System.out.println("Connected: " + s + " " + s.getClass());
			printSocketInfo(s);
			s.getOutputStream().write(("Hello! "+ctr++).getBytes(StandardCharsets.UTF_8));
			s.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally
		{
			try {
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void printSocketInfo(SSLSocket s) {
		System.out.println("Socket class: " + s.getClass());
		System.out.println("   Remote address = " + s.getInetAddress().toString());
		System.out.println("   Remote port = " + s.getPort());
		System.out.println("   Local socket address = " + s.getLocalSocketAddress().toString());
		System.out.println("   Local address = " + s.getLocalAddress().toString());
		System.out.println("   Local port = " + s.getLocalPort());
		System.out.println("   Need client authentication = " + s.getNeedClientAuth());
		SSLSession ss = s.getSession();
		System.out.println("   Cipher suite = " + ss.getCipherSuite());
		System.out.println("   Protocol = " + ss.getProtocol());
	}

}
