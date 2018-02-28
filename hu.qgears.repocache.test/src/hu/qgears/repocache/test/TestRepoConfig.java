package hu.qgears.repocache.test;

import java.io.File;
import java.io.IOException;

import hu.qgears.repocache.config.RepoConfiguration;

/**
 * Provides a no-arg constructor for {@link RepoConfiguration} for convenient
 * testing. 
 * 
 * @author chreex
 */
public class TestRepoConfig extends RepoConfiguration {
	public TestRepoConfig() throws IOException {
		super(createTempDir());
	}
	
	private static File createTempDir() throws IOException {
		final File tempDir = File.createTempFile("repocache", "test");
		
		tempDir.mkdirs();

		return tempDir;
	}
}
