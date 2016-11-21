package hu.qgears.repocache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import hu.qgears.commons.UtilFile;

public class QueryResponseByteArray extends QueryResponse
{
	private byte[] responseBody;

	public QueryResponseByteArray(String url, byte[] responseBody) throws IOException {
		super(url);
		this.responseBody = responseBody;
	}
	public QueryResponseByteArray(String url, byte[] responseBody, boolean folder) throws IOException {
		super(url);
		this.responseBody = responseBody;
		this.folder=folder;
	}

	@Override
	public void saveToFile(File workingCopyFile) throws IOException {
		UtilFile.saveAsFile(workingCopyFile, responseBody);
	}

	@Override
	public InputStream openInputStream() throws FileNotFoundException {
		return new ByteArrayInputStream(responseBody);
	}

	@Override
	public void updateContent(byte[] bytes) throws IOException {
		responseBody=bytes;
	}

	@Override
	public String getResponseAsString() throws FileNotFoundException, IOException {
		return new String(responseBody, StandardCharsets.UTF_8);
	}

	@Override
	public byte[] getResponseAsBytes() throws FileNotFoundException, IOException {
		return responseBody;
	}

	@Override
	public void close() {
		// Nothing to do
	}

	@Override
	public void streamTo(OutputStream outputStream) throws IOException {
		outputStream.write(responseBody);
	}
}
