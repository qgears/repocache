package hu.qgears.repocache.test;

import java.io.File;
import java.io.IOException;

import org.junit.rules.TemporaryFolder;

import hu.qgears.repocache.CommandLineArgs;
import hu.qgears.repocache.RepoCache;

public class TestRepoCache extends RepoCache {
	private Thread startupThread;
	
	public static CommandLineArgs createCommandLineArgs(final TemporaryFolder tempDir) 
			throws IOException {
		final CommandLineArgs commandLineArgs = new CommandLineArgs();
		
		commandLineArgs.configFolder = tempDir.newFolder("config"); 
		commandLineArgs.repo = new File(tempDir.getRoot(), "repo");
		/* Proxy port will be randomized. */
		commandLineArgs.port = 0;
		
		return commandLineArgs;
	}
	
	public TestRepoCache(final TemporaryFolder tempDir) throws IOException {
		super(createCommandLineArgs(tempDir));
	}
	
	@Override
	public void start() throws Exception {
		startupThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					TestRepoCache.super.start();
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		startupThread.start();
	}
}
