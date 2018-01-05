package hu.qgears.repocache.https;

import java.io.InputStream;
import java.io.OutputStream;

public interface IConnection extends AutoCloseable {
	public void connectStreams(InputStream is, OutputStream os) throws Exception;
	@Override
	void close();
}
