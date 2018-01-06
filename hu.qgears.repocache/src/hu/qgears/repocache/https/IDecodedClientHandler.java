package hu.qgears.repocache.https;

import java.net.Socket;

public interface IDecodedClientHandler {
	void handleDecodedClient(Socket client, String targethost, int targetport);
}
