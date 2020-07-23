package hu.qgears.repocache.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;

public class Main
{
    public static void main(String[] args)
    {
        MyDumpHandler dump = new MyDumpHandler();

        Server server = new Server(8080);

        // Specify the Session ID Manager
        DefaultSessionIdManager sessionIdManager = new DefaultSessionIdManager(server);
		server.setSessionIdManager(sessionIdManager);

        // Sessions are bound to a context.
        ContextHandler context = new ContextHandler("/");
        server.setHandler(context);

        // Create the SessionHandler (wrapper) to handle the sessions
        
        SessionHandler sessions = new SessionHandler();
        sessions.setSessionIdManager(sessionIdManager);
        context.setHandler(sessions);

        // Put dump inside of SessionHandler 
        sessions.setHandler(dump);

        // Tree is now
        // Server
        //   + ContextHandler("/")
        //       + SessionHandler(Hash)
        //           + MyDumpHandler

        try
        {
            server.start();
            server.join();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
