package hu.qgears.repocache.ssh;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.UtilFile;

/**
 * Example SSL server that bridges to a plaintext port.
 */
public class ExampleSSL2 {
	public static void main(String[] args) throws Exception {
		SSLServerSocketFactory ssf = SSLContextFactory.createContext(new File("/path/to/pkcs"), "verysec", "site");
		SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket();
		ss.bind(new InetSocketAddress("0.0.0.0", 9001));
		/*
		 * String[] protocols = ss.getSupportedProtocols(); for (int i = 0; i <
		 * protocols.length; i++) { if (protocols[i].equals("SSLv2Hello")) {
		 * continue; } SSLContext sslc = SSLContext.getInstance(protocols[i]);
		 * SSLSessionContext sslsc = sslc.getServerSessionContext();
		 * System.out.println("Protocol: " + protocols[i]);
		 * sslsc.setSessionTimeout(Integer.MAX_VALUE); int newtime =
		 * sslsc.getSessionTimeout(); if (newtime != Integer.MAX_VALUE) { throw
		 * new Exception ("Expected timeout: " + Integer.MAX_VALUE +
		 * ", got instead: " + newtime); } }
		 */
//		SSLSession session=ssf.
		while (true) {
			final SSLSocket s = (SSLSocket) ss.accept();
			new Thread() {
				public void run() {
					handleSocket(s);
				};
			}.start();
		}

	}
	protected static void handleSocket(SSLSocket s) {
		try {
//			System.out.println("Connected: " + s + " " + s.getClass());
			printSocketInfo(s);
			try(Socket cs=new Socket("localhost", 8888))
			{
				ConnectStreams.startStreamThread(s.getInputStream(), cs.getOutputStream(), false, UtilFile.defaultBufferSize.get());
				ConnectStreams.doStream(cs.getInputStream(), s.getOutputStream());
			}finally
			{
				s.close();
			}
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
