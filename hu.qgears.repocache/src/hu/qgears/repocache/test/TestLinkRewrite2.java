package hu.qgears.repocache.test;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.httpget.HttpClientToInternet;
import hu.qgears.repocache.httpget.HttpGet;

public class TestLinkRewrite2 {
	public static void main(String[] args) throws HttpException, IOException {
		HttpClientToInternet http= new HttpClientToInternet();
		QueryResponse resp=http.get(new HttpGet("http://qgears.com/opensource/updates/old-repo/2.0/"));
		System.out.println(new String(resp.responseBody));
	}
}
