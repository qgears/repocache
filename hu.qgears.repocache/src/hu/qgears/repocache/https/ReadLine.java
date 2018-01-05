package hu.qgears.repocache.https;

import java.io.IOException;
import java.io.InputStream;

public class ReadLine {
	public static String readLine(InputStream is, int maxLength) throws IOException
	{
		StringBuilder ret=new StringBuilder();
		int c=is.read();
		while(c!=-1&&c!='\n')
		{
			if(c!='\r')
			{
				ret.append((char)c);
			}
			if(ret.length()>maxLength)
			{
				throw new IOException("Line size is greater than "+maxLength);
			}
			c=is.read();
		}
		return ret.toString();
	}
}
