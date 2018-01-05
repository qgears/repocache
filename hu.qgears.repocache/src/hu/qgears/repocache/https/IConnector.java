package hu.qgears.repocache.https;

public interface IConnector {

	IConnection connect(String targethost, int targetport) throws NoConnectException;

}
