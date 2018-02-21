package hu.qgears.repocache.test;

import java.io.File;

import hu.qgears.repocache.httpget.HttpGet;
import hu.qgears.repocache.httpget.StreamingHttpClient;

public class TestStreamingDownload {
	public static void main(String[] args) throws Exception {
		new StreamingHttpClient().get(new HttpGet(new File("/tmp/a.txt"), 
				"http://localhost:8080/a.txt", new TestRepoConfig()));
	}
}
