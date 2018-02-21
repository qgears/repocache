package hu.qgears.repocache.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import hu.qgears.repocache.RepoCache;
import hu.qgears.repocache.config.RepoConfiguration;

/**
 * Tests the HTTP connection and socket timeouts.
 * @author chreex
 */
public class TestHttpTimeouts {
	private static final Logger LOGGER = Logger.getLogger(
			TestHttpTimeouts.class.getSimpleName());
	
	/**
	 * Test-purpose, non-routable URL for testing connection timeout. More
	 * information:  
	 */
	private static final String CONNTIMEOUT_TEST_URL_PREFIX = "http://10.255.255.1/";
	
	/** Test HTTP-server. */
	private TestHttpServer testServer;
	/** Tested {@link RepoCache} instance. */
	private RepoCache repoCache;
	
	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	@Before
	public void beforeTest() throws Exception {
		testServer = new TestHttpServer();
		testServer.start();
		LOGGER.info("Test server started on port " + testServer.getPort());
		
		repoCache = new TestRepoCache(tempDir);
		repoCache.start();
		repoCache.waitForStartup();
	}
	
	@After
	public void afterTest() throws Exception {
		// TODO graceful shutdown of repocache test instance
		testServer.stop();
	}
	
	/**
	 * Tests the effect of setting the
	 * {@link RepoConfiguration#setHttpConnectionTimeoutMs(int) HTTP connection timeout}.
	 * The proxy server is expected to pass the error status on the timeout to
	 * the client.
	 * @throws IOException 
	 * @throws MalformedURLException not expected  
	 * @throws InterruptedException 
	 */
	@Test// (expected = IOException.class)
	public void testConnectionTimeout() 
			throws MalformedURLException, IOException, InterruptedException {
		repoCache.getConfiguration().setHttpConnectionTimeoutMs(5000);
		
		TimeUnit.SECONDS.sleep(60);
		
		/* No timeout set in the client. */
		TestHttpClient.download(CONNTIMEOUT_TEST_URL_PREFIX,
				repoCache.getPort());
		
	}
	
	/**
	 * Tests the effect of setting the
	 * {@link RepoConfiguration#setHttpSoTimeoutMs(int) HTTP socket timeout}.
	 */
	@Test
	@Ignore
	public void testSoTimeout() {
		repoCache.getConfiguration().setHttpSoTimeoutMs(100);
	}
}
