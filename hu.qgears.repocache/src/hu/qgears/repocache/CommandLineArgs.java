package hu.qgears.repocache;

import java.io.File;
import java.io.IOException;

import hu.qgears.commons.UtilFile;
import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;

public class CommandLineArgs {
	private byte[] configOverride;
	@JOHelp("Folder that contains the chaced git repository. If not exists then it is created.")
	public File repo;
	@JOHelp("Configuration xml file.")
	public File config;
	@JOHelp("Http server port that is opened by the server")
	public int port=8080;
	@JOHelp("If local only then the cache does not access the remote servers at all.")
	@JOSimpleBoolean
	public boolean localOnly;
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
		if(repo==null)
		{
			throw new IllegalArgumentException("'repo' Repository folder must be set as a command line parameter.");
		}
		if(repo.exists()&& !repo.isDirectory())
		{
			throw new IllegalArgumentException("'repo' Repository folder must be a folder.");
		}
	}
	public CommandLineArgs setConfigOverride(byte[] bs)
	{
		this.configOverride=bs;
		return this;
	}
	public byte[] openConfigXml() throws IOException {
		if(configOverride!=null)
		{
			return configOverride;
		}
		return UtilFile.loadFile(config);
	}
}
