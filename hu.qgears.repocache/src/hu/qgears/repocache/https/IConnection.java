package hu.qgears.repocache.https;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public interface IConnection extends AutoCloseable {
	public void connectStreams(Socket c, InputStream is, OutputStream os) throws Exception;
	@Override
	void close();
}
