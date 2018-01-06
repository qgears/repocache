package hu.qgears.repocache.https;

import java.util.HashMap;
import java.util.Map;

public class HttpsProxyConnectionsManager {
	public class RegistryEntry implements AutoCloseable
	{
		public int localPort;
		public String servername;
		public int port;
		public boolean rwMode;
		public volatile boolean closed;
		
		public RegistryEntry(int localPort, String servername, int port, boolean rwMode) {
			super();
			this.localPort=localPort;
			this.servername = servername;
			this.port = port;
			this.rwMode = rwMode;
		}

		@Override
		public void close() {
			closed=true;
			synchronized (map) {
				RegistryEntry e=map.get(localPort);
				if(e==this)
				{
					map.remove(localPort);
				}
			}
		}	
	}
	private Map<Integer, RegistryEntry> map=new HashMap<>();
	private static final HttpsProxyConnectionsManager instance=new HttpsProxyConnectionsManager();
	public static HttpsProxyConnectionsManager getInstance() {
		return instance;
	}
	public RegistryEntry register(int localPort, String targethost, int targetport, boolean rwMode) {
		RegistryEntry ret=new RegistryEntry(localPort, targethost, targetport, rwMode);
		synchronized (map) {
			map.put(localPort, ret);
		}
		return ret;
	}
	public RegistryEntry get(int remotePort) {
		synchronized (map) {
			return map.get(remotePort);
		}
	}
}
