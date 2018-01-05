package hu.qgears.repocache.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Create an SSL context factory based on an existing plcs12 file dynamically.
 */
public class SSLContextFactory {
	public static SSLServerSocketFactory createContext(File pkcs12, String passwd, String alias) throws Exception {
		// LOAD EXTERNAL KEY STORE
		KeyStore mstkst;
		mstkst = KeyStore.getInstance("pkcs12");
		mstkst.load(new FileInputStream(pkcs12), passwd.toCharArray());
		// CREATE EPHEMERAL KEYSTORE FOR THIS SOCKET USING DESIRED CERTIFICATE
		SSLContext ctx = SSLContext.getInstance("TLS");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		char[] blkpwd = new char[0];

		KeyStore sktkst = KeyStore.getInstance("jks");
		sktkst.load(null, blkpwd);
		Key key=mstkst.getKey(alias, passwd.toCharArray());
		sktkst.setKeyEntry(alias, key, blkpwd, mstkst.getCertificateChain(alias));
		kmf.init(sktkst, blkpwd);
		ctx.init(kmf.getKeyManagers(), null, null);
		return ctx.getServerSocketFactory();
	}
}
