package hu.qgears.repocache.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.httpget.HttpGet;
import hu.qgears.repocache.httpget.StreamingHttpClient;

public class TestLinkRewrite2 {
	public static void main(String[] args) throws HttpException, IOException {
		StreamingHttpClient http= new StreamingHttpClient();
		QueryResponse resp=http.get(new HttpGet(new File("/tmp/linkrewrite.txt"), 
				"http://qgears.com/opensource/updates/old-repo/2.0/", 
				new TestRepoConfig()));
		System.out.println(resp.getResponseAsString());
	}
}
