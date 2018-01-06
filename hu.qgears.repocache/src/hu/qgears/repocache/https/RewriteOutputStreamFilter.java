package hu.qgears.repocache.https;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RewriteOutputStreamFilter extends OutputStream
{
	public static final String headerName="QGEARS-REPOCACHE-SSL-PROXY";
	private boolean rewritten;
	private String host;
	private int port;
	private String rewriteInfo;
	private OutputStream out;
	
	private ByteArrayOutputStream firstLine=new ByteArrayOutputStream();
	
	public RewriteOutputStreamFilter(OutputStream out, String host, int port, String rewriteInfo) {
		super();
		this.out = out;
		this.host=host;
		this.port=port;
		this.rewriteInfo=rewriteInfo;
	}
	@Override
	public void write(int b) throws IOException {
		if(rewritten)
		{
			out.write(b);
		}else
		{
			if(b!='\n')
			{
				firstLine.write(b);
				out.write(b);
				if(firstLine.size()>HttpsProxyServer.headerMaxLength)
				{
					throw new IOException("Header length more than "+HttpsProxyServer.headerMaxLength);
				}
			}else
			{
				out.write(b);
				out.write((RewriteOutputStreamFilter.headerName+": https "+host+" "+port+" "+rewriteInfo+"\r\n").getBytes(StandardCharsets.UTF_8));
				rewritten=true;
			}
		}
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if(rewritten)
		{
			out.write(b, off, len);
		}else
		{
			super.write(b, off, len);
		}
	}
	@Override
	public void write(byte[] b) throws IOException {
		if(rewritten)
		{
			out.write(b);
		}else
		{
			super.write(b);
		}
	}
	public void flush() throws IOException {
		out.flush();
	};
	@Override
	public void close() throws IOException {
		out.close();
		super.close();
	}
}
