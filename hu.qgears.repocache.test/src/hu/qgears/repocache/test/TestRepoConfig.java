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
		super(File.createTempFile("repocache", "test", 
				new File("java.io.tmpdir")));
	}
}
