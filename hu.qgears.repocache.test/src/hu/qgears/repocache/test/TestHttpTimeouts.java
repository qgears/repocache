package hu.qgears.repocache.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
	
	private static final String SOTIMEOUT_TEST_URL_PREFIX_TEMPLATE = 
			"http://127.0.0.1:%d/sotimeout.bin";
	
	/** Test HTTP-server. */
	private TestHttpServer testServer;
	/** Tested {@link RepoCache} instance. */
	private RepoCache repoCache;
	
	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();
	
	@Rule
	public ExpectedException gatewayTimeout = ExpectedException.none();

	/**
	 * Sets up a test-purpose HTTP server on the localhost and the tested
	 * repocache instance. This method is synchronized: it waits for the 
	 * repocache to be started..
	 * @throws Exception if any problem happens during starting up the test HTTP
	 * server or the tested repocache instance
	 */
	@Before
	public void beforeTest() throws Exception {
		testServer = new TestHttpServer();
		testServer.start();
		LOGGER.info("Test server started on port " + testServer.getPort());
		
		repoCache = new TestRepoCache(tempDir);
		repoCache.start();
		repoCache.waitForStartup();
		repoCache.getConfiguration().setAccessRules(
				"transparent /proxy/http/10.255.255.1\n"
				+ "transparent /proxy/http/127.0.0.1");
	}
	
	@After
	public void afterTest() throws Exception {
		// TODO graceful shutdown of repocache test instance
		repoCache.stop();
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
	private void expectGatewayTimeout(final String messageFragment) {
		gatewayTimeout.expect(IOException.class);
		gatewayTimeout.expectMessage(messageFragment);
	}
	
	@Test
	public void testConnectionTimeout() 
			throws MalformedURLException, IOException, InterruptedException {
		repoCache.getConfiguration().setHttpConnectionTimeoutMs(7000);
		
		TimeUnit.SECONDS.sleep(2);
		
		expectGatewayTimeout("Server returned HTTP response code: "
				+ HttpServletResponse.SC_GATEWAY_TIMEOUT);
		
		/* No timeout set in the client. */
		TestHttpClient.download(CONNTIMEOUT_TEST_URL_PREFIX,
				repoCache.getProxyPort());
	}

	/**
	 * Tests the effect of setting the
	 * {@link RepoConfiguration#setHttpSoTimeoutMs(int) HTTP socket timeout}.
	 * @throws IOException not expected
	 * @throws MalformedURLException not expected 
	 */
	@Test
	public void testSoTimeout() throws MalformedURLException, IOException {
		repoCache.getConfiguration().setHttpSoTimeoutMs(3000);
		
		expectGatewayTimeout("Server returned HTTP response code: "
				+ HttpServletResponse.SC_GATEWAY_TIMEOUT);
		
		TestHttpClient.download(String.format(SOTIMEOUT_TEST_URL_PREFIX_TEMPLATE, 
				testServer.getPort()), repoCache.getProxyPort());
	}
}
