package hu.qgears.repocache.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jetty.util.MultiMap;

public interface IMultipartHandler {

	/**
	 * 
	 * @param name
	 * @param filename
	 * @param headers
	 * @param contentType
	 * @return the returned output stream should be buffered because it is written byte-by byte.
	 * @throws IOException
	 */
	OutputStream createPart(String name, String filename, MultiMap<String> headers, String contentType) throws IOException;

}
