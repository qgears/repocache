package hu.qgears.repocache.ssh;

import java.io.File;
import java.io.IOException;

import javax.net.ssl.SSLServerSocketFactory;

import hu.qgears.commons.UtilProcess;

/**
 * Launch an SSL site with a dynamic certificate
 * @author rizsi
 *
 */
public class SSLDynamicCert
{
	private final File certsFolder;
	private final Object lockObject=new Object();
	private final String repocacheHostName;
	
	public SSLDynamicCert(File certsFolder, String repocacheHostName)
	{
		this.certsFolder=certsFolder;
		this.repocacheHostName = repocacheHostName;
	}
	
	public SSLServerSocketFactory openServer(String hostName) throws Exception
	{
		validateHostname(hostName);
		File pkfile=new File(certsFolder, "keys/"+hostName+"/site.p12");
		synchronized (lockObject) {
			if(!pkfile.exists())
			{
				File script=new File(certsFolder, "dynamiccert.sh");
				ProcessBuilder pb=new ProcessBuilder(new String[] {
						script.getAbsolutePath(), hostName, repocacheHostName
				}).directory(certsFolder);
				Process p=pb.start();
				String s=UtilProcess.execute(p);
			}
		}
		SSLServerSocketFactory fact=SSLContextFactory.createContext(pkfile, "verysec", "site");
		return fact;
	}
	private void validateHostname(String hostName) throws IOException {
		if(hostName.contains("/"))
		{
			throw new IOException("Invalid hostname: "+hostName);
		}
	}
}
