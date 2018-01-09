package hu.qgears.repocache;

import java.io.File;

import hu.qgears.repocache.ssh.SSLDynamicCert;
import hu.qgears.tools.AbstractTool.IArgs;
import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;

public class CommandLineArgs implements IArgs
{
	@JOHelp("Configuration folder")
	public File configFolder;
	@JOHelp("Folder that contains the cached git repository. If not exists then it is created.")
	public File repo;
	@JOHelp("Http server port that is opened by the server")
	public int port=8080;
	@JOHelp("Http proxy server port that is opened by the server")
	public int proxyPort=-1;
	@JOHelp("Https proxy server port that is opened by the server")
	public int httpsProxyPort=-1;
	@JOHelp("If local only then the cache does not access the remote servers at all.")
	@JOSimpleBoolean
	public boolean localOnly;
	@JOHelp("Downloads folder. Should be on the same physical device as the repo so files can be moved into repo by cheap move command.")
	public File downloadsFolder;
	@JOHelp("Configure log4j to log to console.")
	@JOSimpleBoolean
	public boolean log4jToConsole;
	@JOHelp("Bind the public server ports to this address.")
	public String serverHost="0.0.0.0";
	/**
	 * Validate parameters if they are valid to start the repo cache server.
	 */
	public void validate()
	{
		if(repo==null)
		{
			throw new IllegalArgumentException("'repo' Repository folder must be set as a command line parameter.");
		}
		if(repo.exists()&& !repo.isDirectory())
		{
			throw new IllegalArgumentException("'repo' Repository folder must be a folder.");
		}
		if(downloadsFolder==null)
		{
			throw new IllegalArgumentException("'downloadsFolder' must be set as command line parameter.");
		}
		if(!downloadsFolder.exists())
		{
			downloadsFolder.mkdirs();
		}
		if(!downloadsFolder.isDirectory())
		{
			throw new IllegalArgumentException("'downloadsFolder' must be a directory.");
		}
	}
	public boolean hasProxyPortDefined() {
		return proxyPort>-1;
	}
	public boolean hasHttpsProxyPortDefined() {
		return httpsProxyPort>-1;
	}
	public Integer getProxyPort() {
		return hasProxyPortDefined() ? proxyPort+1 : null;
	}
	public Integer getHttpsProxyPort() {
		return hasHttpsProxyPortDefined()? httpsProxyPort: null;
	}
	private SSLDynamicCert dynamicCertSupplier;
	synchronized public SSLDynamicCert getDynamicCertSupplier()
	{
		if(dynamicCertSupplier==null)
		{
			dynamicCertSupplier=new SSLDynamicCert(getCertsFolder());
		}
		return dynamicCertSupplier;
	}
	public File getCertsFolder() {
		return new File(configFolder, "certs");
	}
}
