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
	public SSLDynamicCert(File certsFolder)
	{
		this.certsFolder=certsFolder;
	}
	private File certsFolder;
	private Object lockObject=new Object();
	public SSLServerSocketFactory openServer(String hostName) throws Exception
	{
		validateHostname(hostName);
		File pkfile=new File(certsFolder, "keys/"+hostName+"/site.p12");
		synchronized (lockObject) {
			if(!pkfile.exists())
			{
				File script=new File(certsFolder, "dynamiccert.sh");
				System.out.println("Site does not exist yet! '"+hostName+"' '"+ pkfile.getAbsolutePath()+"'");
				ProcessBuilder pb=new ProcessBuilder(new String[]{script.getAbsolutePath(), hostName}).directory(certsFolder);
				Process p=pb.start();
				String s=UtilProcess.execute(p);
				System.out.println("Process output: "+s);
			}else
			{
				System.out.println("Site already exists! '"+hostName+"' '"+ pkfile.getAbsolutePath()+"'");
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
