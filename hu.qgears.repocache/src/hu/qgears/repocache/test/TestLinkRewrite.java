package hu.qgears.repocache.test;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.httpget.HttpClientToInternet;
import hu.qgears.repocache.httpget.HttpGet;

public class TestLinkRewrite {
	public static void main(String[] args) throws HttpException, IOException {
		HttpClientToInternet http= new HttpClientToInternet();
		QueryResponse resp=http.get(new HttpGet("https://repo.eclipse.org/content/groups/emf-incquery/"));
		System.out.println(new String(resp.responseBody));
	}
}
