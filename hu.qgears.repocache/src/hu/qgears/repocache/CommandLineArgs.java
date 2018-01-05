package hu.qgears.repocache;

import java.io.File;
import java.io.IOException;

import hu.qgears.commons.UtilFile;
import hu.qgears.repocache.ssh.SSLDynamicCert;
import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;

public class CommandLineArgs {
	private byte[] configOverride;
	private byte[] repoModeConfigOverride;
	@JOHelp("Folder that contains the cached git repository. If not exists then it is created.")
	public File repo;
	@JOHelp("Configuration xml file.")
	public File config;
	@JOHelp("Repo mode configuration xml file.")
	public File repoModeConfig;
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
	@JOHelp("Fake Certificates folder used to Man In The Middle https queries.")
	public File certsFolder;
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
		if(configOverride==null)
		{
			if(config==null)
			{
				throw new IllegalArgumentException("config file must be set as a command line parameter.");
			}
			if((!config.exists()) || !config.isFile())
			{
				throw new IllegalArgumentException("config file does not exist.");
			}
		}
		if(repoModeConfigOverride==null)
		{
			if(repoModeConfig==null)
			{
				throw new IllegalArgumentException("repo mode config file must be set as a command line parameter.");
			}
		}
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
	public CommandLineArgs setConfigOverride(byte[] bs)
	{
		this.configOverride=bs;
		return this;
	}
	public CommandLineArgs setRepoModeConfigOverride(byte[] bs, String filePath)
	{
		this.repoModeConfigOverride=bs;
		this.repoModeConfig=new File(filePath);
		return this;
	}
	public byte[] openConfigXml() throws IOException {
		if(configOverride!=null)
		{
			return configOverride;
		}
		return UtilFile.loadFile(config);
	}
	public byte[] openRepoModeConfigXml() {
		if(repoModeConfigOverride!=null)
		{
			return repoModeConfigOverride;
		}
		byte[] ret = null;
		try {
			ret = UtilFile.loadFile(repoModeConfig);
		} catch (IOException e) {
			System.out.println("Repomode config file missing.");
		}
		return ret;
	}
	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}
	public boolean hasProxyPortDefined() {
		return proxyPort>-1;
	}
	public boolean hasHttpsProxyPortDefined() {
		return httpsProxyPort>-1;
	}
	public Integer getProxyPortReadonly() {
		return hasProxyPortDefined() ? proxyPort : null;
	}
	public Integer getProxyPortUpdate() {
		return hasProxyPortDefined() ? proxyPort+1 : null;
	}
	public Integer getHttpsProxyPortReadonly() {
		return hasHttpsProxyPortDefined()? httpsProxyPort: null;
	}
	public Integer getHttpsProxyPortUpdate() {
		return hasHttpsProxyPortDefined()? httpsProxyPort+1: null;
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
