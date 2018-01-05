package hu.qgears.repocache.https;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.Scheduler;

public class JettyInProcessConnector extends AbstractConnector{

	class Transport
	{
		
	}
	public JettyInProcessConnector(Server server, Executor executor, Scheduler scheduler, ByteBufferPool pool,
			int acceptors, ConnectionFactory[] factories) {
		super(server, executor, scheduler, pool, acceptors, factories);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getTransport() {
		return new Transport();
	}

	@Override
	protected void accept(int acceptorID) throws IOException, InterruptedException {
		System.out.println("Accept!");
	}

}
