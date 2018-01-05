package hu.qgears.repocache.https;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class Connection implements IConnection, Runnable
{
	private Socket tg;
	private String name;
	private Socket c;
	private boolean log;
	public Connection(String name, Socket tg, boolean log) {
		super();
		this.name=name;
		this.tg = tg;
		this.log=log;
	}
	@Override
	public void connectStreams(Socket c, InputStream is, OutputStream os) throws Exception {
		this.c=c;
		HttpsConnectStreams t1=new HttpsConnectStreams(name+" send", is, tg.getOutputStream()).setAfterCallback(this).setOs2(createLogger()).start();
		HttpsConnectStreams t2=new HttpsConnectStreams(name+" receive", tg.getInputStream(), os).setAfterCallback(this).setOs2(createLogger()).start();
		t1.join();
		t2.join();
		if(log)
		{
			System.err.println("---------------CONN CLOSED -------------");
		}
	}
	private OutputStream createLogger() {
		return log?System.err:null;
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
	@Override
	public void run() {
		try {
			c.close();
		} catch (IOException e) {
		}
		try {
			tg.close();
		} catch (IOException e) {
		}
	}
}
