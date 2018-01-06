package hu.qgears.repocache.https;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import hu.qgears.commons.UtilString;
import hu.qgears.commons.signal.SignalFutureWrapper;

/**
 * Simple plain TCP server based HTTPS proxy implementation.
 * Only handles the HTTP CONNECT command.
 * Connection to the remote server is handled by the connector plugin object.
 */
public class HttpsProxyServer extends Thread{
	private String host;
	private int port;
	private IConnector connector;
	
	public final SignalFutureWrapper<HttpsProxyServer> started=new SignalFutureWrapper<>();
	public final SignalFutureWrapper<HttpsProxyServer> stopped=new SignalFutureWrapper<>();
	
	public HttpsProxyServer(String host, int port, IConnector connector) {
		super();
		this.connector=connector;
		this.host = host;
		this.port = port;
	}
	private volatile boolean exit=false;
	public static final int headerMaxLength=8192;
	private ServerSocket ss;
	public void run()
	{
		try {
			ss=new ServerSocket();
			try
			{
				ss.bind(new InetSocketAddress(host, port));
				started.ready(this, null);
				while(!exit)
				{
					Socket s=ss.accept();
					handle(s);
				}
			}finally
			{
				ss.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally
		{
			stopped.ready(this, null);
		}
	}
	private void handle(final Socket s) {
		new Thread("Handle HTTPS proxy query")
		{
			public void run() {
				try {
					handleThread(s);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
	}
	protected void handleThread(Socket s) throws Exception {
		try {
			InputStream is=s.getInputStream();
			OutputStream os=s.getOutputStream();
			try {
				String query=ReadLine.readLine(is, headerMaxLength);
				List<String> pieces=UtilString.split(query, " ");
				if(pieces.size()<1)
				{
					return;
				}
				String command=pieces.get(0);
				if(!"CONNECT".equals(command))
				{
					throw new HeaderException(400, "Bad Request CONNECT COMMAND is expected");
				}
				String hostaccess=pieces.get(1);
				List<String> portpieces=UtilString.split(hostaccess, ":");
				String targethost=portpieces.get(0);
				int targetport=443;
				if(portpieces.size()>1)
				{
					targetport=Integer.parseInt(portpieces.get(1));
				}
				if(portpieces.size()>2)
				{
					throw new HeaderException(400, "Bad Request error parsing host");
				}
				int nLine=0;
				String line=ReadLine.readLine(is, headerMaxLength);
				while(line.length()>0)
				{
					//System.out.println("param: "+line);
					line=ReadLine.readLine(is, headerMaxLength);
					nLine++;
					if(nLine>headerMaxLength)
					{
						throw new HeaderException(400, "Bad Request error too many lines");
					}
				}
				try {
					try(IConnection c=connector.connect(targethost, targetport))
					{
						sendReply(os, 200, "Connection Established");
						c.connectStreams(s, is, os);
					}
				} catch (NoConnectException e1) {
					sendReply(os, 404, "Not found can not connect target");
				}
			} catch (HeaderException e) {
				sendReply(os, e.code, e.message);
				throw new IOException(e);
			}
		}finally
		{
			s.close();
		}
	}
	private void sendReply(OutputStream os, int i, String string) throws IOException {
		// Example: HTTP/1.1 200 Connection Established
		os.write("HTTP/1.1 ".getBytes(StandardCharsets.US_ASCII));
		os.write(Integer.toString(i).getBytes(StandardCharsets.US_ASCII));
		os.write(" ".getBytes(StandardCharsets.US_ASCII));
		os.write(string.getBytes(StandardCharsets.US_ASCII));
		os.write("\n".getBytes(StandardCharsets.US_ASCII));
		os.write("\n".getBytes(StandardCharsets.US_ASCII));
		os.flush();
	}
	public void stopServer() {
		exit=true;
		try {
			ss.close();
		} catch (IOException e) {
		}
	}
}
