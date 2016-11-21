package hu.qgears.repocache.test;

import java.io.File;

import hu.qgears.repocache.httpget.HttpGet;
import hu.qgears.repocache.httpget.StreamingHttpClient;

public class TestStreamingDownload {
	public static void main(String[] args) throws Exception {
		new StreamingHttpClient().get(new HttpGet(new File("/tmp/a.txt"), "http://localhost:8080/a.txt"));
//		// Create an instance of HttpClient.
//		HttpClient client = new HttpClient();
//
//		// Create a method instance.
//		GetMethod method = new GetMethod();
////		GetMethod method = new GetMethod("http://releases.ubuntu.com/16.04.1/ubuntu-16.04.1-desktop-amd64.iso");
//		
//
//		// Provide custom retry handler is necessary
//		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
//
//		try(DownloadLogAndTimeout log=new DownloadLogAndTimeout(method)) {
//			// Execute the method.
//			int statusCode = client.executeMethod(method);
//
//			if (statusCode != HttpStatus.SC_OK) {
//				throw new FileNotFoundException("Method failed: " + method.getStatusLine());
//			}
//			long l=-1;
//			Header h=method.getResponseHeader("Content-Length");
//			if(h!=null)
//			{
//				try {
//					l=Long.parseLong(h.getValue());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			System.out.println("Content length: '"+(h==null?"null":h.getValue())+"'");
//			System.out.flush();
//			try(FileOutputStream fos=new FileOutputStream("/tmp/a.txt"))
//			{
//				try(InputStream is=method.getResponseBodyAsStream())
//				{
//					long sum=0;
//					byte[] buffer=new byte[1000000];
//					int n;
//					while((n=is.read(buffer))>0)
//					{
//						sum+=n;
//						log.progress(sum, l);
//						fos.write(buffer, 0, n);
//					}
//				}
//			}
////			return new QueryResponse(method, get);
//		} finally {
//			method.abort();
//			// Release the connection.
//			method.releaseConnection();
//		}
	}
}
