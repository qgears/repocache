package hu.qgears.repocache.transparentproxy;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLSocketFactory;

import hu.qgears.commons.StreamTee;
import hu.qgears.repocache.https.Connection;
import hu.qgears.repocache.https.IDecodedClientHandler;

public class DecodedClientHandlerToRealTarget implements IDecodedClientHandler {

	private OutputStream logStream;
	
	public DecodedClientHandlerToRealTarget(OutputStream logStream) {
		super();
		this.logStream = logStream;
	}

	@Override
	public void handleDecodedClient(Socket client, String targethost, int targetport) {
		try {
			Socket s=SSLSocketFactory.getDefault().createSocket(targethost, targetport);
			StreamTee tee=new StreamTee(s.getOutputStream(), true, logStream, false);
			System.err.println("Connected to: "+targethost+":"+targetport);
			logStream.write(("CONNECT TO SERVER "+targethost+":"+targetport+"\n").getBytes(StandardCharsets.UTF_8));
			try(Connection c=new Connection("SSL plaintext "+targethost+":"+targetport, s, tee, s.getInputStream(), false))
			{
				c.connectStreams(client, client.getInputStream(), client.getOutputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
