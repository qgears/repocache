package hu.qgears.repocache.test;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.httpget.HttpGet;
import hu.qgears.repocache.httpget.StreamingHttpClient;

/**
 * 
 * 
 * @author akos
 */
public class TestStreamingDownload {
	private TestServer testServer;

	@Before
	public void beforeTest() throws InterruptedException {
		testServer = new TestServer();
		new Thread(new Runnable() {
			public void run() {
				try {
					testServer.startHttpServer();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		
		testServer.waitForStartup();
	}
	
	@After
	public void afterTest() throws Exception {
		testServer.stopHttpServer();
	}
	
	@Test
	public void testDownload() throws Exception {
		final QueryResponse queryResponse = new StreamingHttpClient().get(
				new HttpGet(new File("/tmp/a.txt"), 
						"http://localhost:8081/a.txt", new TestRepoConfig()));
		
		Assert.assertEquals(TestServer.PAYLOAD_SIZE_BYTES,
				queryResponse.getResponseAsBytes().length);
	}
}
