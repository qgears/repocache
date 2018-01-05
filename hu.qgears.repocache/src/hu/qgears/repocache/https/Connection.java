package hu.qgears.repocache.https;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import hu.qgears.commons.ConnectStreams;

class Connection implements IConnection
{
	Socket tg;
	public Connection(Socket tg) {
		super();
		this.tg = tg;
	}
	@Override
	public void connectStreams(InputStream is, OutputStream os) throws Exception {
		Thread t1=ConnectStreams.startStreamThread(is, tg.getOutputStream());
		Thread t2=ConnectStreams.startStreamThread(tg.getInputStream(), os);
		t1.join();
		t2.join();
	}
	@Override
	public void close() {
		try {
			tg.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
