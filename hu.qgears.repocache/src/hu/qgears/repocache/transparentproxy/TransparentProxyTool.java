package hu.qgears.repocache.transparentproxy;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.jetty.server.Server;

import hu.qgears.repocache.https.DynamicSSLProxyConnector;
import hu.qgears.repocache.https.HttpsProxyLifecycle;
import hu.qgears.repocache.ssh.SSLDynamicCert;
import hu.qgears.tools.AbstractTool;
import joptsimple.annot.JOHelp;

public class TransparentProxyTool extends AbstractTool
{
	public class Args implements IArgs
	{
		public String host="0.0.0.0";
		public int port=8091;
		public String httpsHost="0.0.0.0";
		public int httpsPort=8090;
		@JOHelp("Fake Certificates folder used to Man In The Middle https queries.")
		public File certsFolder;
		@Override
		public void validate() {
		}
		
		private SSLDynamicCert dynamicCertSupplier;
		synchronized public SSLDynamicCert getDynamicCertSupplier()
		{
			if(dynamicCertSupplier==null)
			{
				if(certsFolder==null)
				{
					throw new RuntimeException("certsFolder is not specified");
				}
				dynamicCertSupplier=new SSLDynamicCert(certsFolder);
			}
			return dynamicCertSupplier;
		}
	}
	@Override
	public String getId() {
		return "transparentproxy";
	}

	@Override
	public String getDescription() {
		return "Transparent http and https proxy with logging capability";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		Args args=(Args)a;
		Server server = new Server();
		server.addBean(new HttpsProxyLifecycle(args.httpsHost, args.httpsPort,
				new DynamicSSLProxyConnector(args.getDynamicCertSupplier(), new DecodedClientHandlerToRealTarget(new FileOutputStream(new File("/tmp/proxylog.txt"))))));
		server.start();
		server.join();
		return 0;
	}

	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}

}
