package hu.qgears.repocache.https;

import java.net.Socket;

public class SimpleConnector implements IConnector
{
	@Override
	public IConnection connect(String targethost, int targetport) throws NoConnectException {
		Socket tg;
		try
		{
			tg=new Socket(targethost, targetport);
			return new Connection("passthrough "+targethost+":"+targetport,tg, tg.getOutputStream(), tg.getInputStream(), false);
		}catch(Exception e)
		{
			throw new NoConnectException(e);
		}
	}
}
