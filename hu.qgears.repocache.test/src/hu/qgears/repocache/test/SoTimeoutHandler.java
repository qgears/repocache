package hu.qgears.repocache.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * HTTP request handler that allows downloading an array of 0xFF bytes with long
 * intermittent puase to test socket timeout handling of HTTP clients.
 * Simple test in command line:<p>
 * <code>wget --tries=1 --read-timeout=3 http://127.0.0.1:20000/probe.bin</code>
 * 
 * @author chreex
 */
public class SoTimeoutHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(SoTimeoutHandler.class.getName());
	
	private static final int DOWNLOAD_SIZE_HALF = 16;
	private static final int SOCKET_WAIT_SECS = 6;
	private static final byte OUT_BUFFER_CONTENT = (byte)0xFF;
	
	@Override
	public void handle(final String target, final Request baseRequest, 
			final HttpServletRequest request, final HttpServletResponse response)
					throws IOException, ServletException {
		response.setContentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType());
		response.setContentLength(DOWNLOAD_SIZE_HALF * 2);
		baseRequest.setHandled(true);
		
		final ServletOutputStream responseOutput = response.getOutputStream();
		final byte[] outBuffer = new byte[DOWNLOAD_SIZE_HALF];
		
		Arrays.fill(outBuffer, OUT_BUFFER_CONTENT);
		
		responseOutput.write(outBuffer);
		responseOutput.flush();
		try {
			TimeUnit.SECONDS.sleep(SOCKET_WAIT_SECS);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (final InterruptedException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			
			LOGGER.log(Level.SEVERE, "Exception during timed wait", e);
		}
		responseOutput.write(outBuffer);
		
		responseOutput.close();
	}

}
