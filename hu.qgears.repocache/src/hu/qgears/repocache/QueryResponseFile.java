package hu.qgears.repocache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import hu.qgears.commons.UtilFile;
import hu.qgears.repocache.httpget.StreamingHttpClient;

public class QueryResponseFile extends QueryResponse{
	private File file;
	private boolean deleteFileOnClose;
	public QueryResponseFile(String url, File file) throws IOException
	{
		super(url);
		this.file = file;
	}
	public QueryResponseFile(String url, File file, boolean folder)
	{
		super(url, folder);
		this.file = file;
	}
	public InputStream openInputStream() throws FileNotFoundException {
		return new FileInputStream(file);
	}
	public void updateContent(byte[] bytes) throws IOException {
		UtilFile.saveAsFile(file, bytes);
	}
	public String getResponseAsString() throws FileNotFoundException, IOException {
		return UtilFile.loadAsString(openInputStream());
	}
	public byte[] getResponseAsBytes() throws FileNotFoundException, IOException {
		return UtilFile.loadFile(openInputStream());
	}
	@Override
	public void saveToFile(File workingCopyFile) throws IOException {
		Files.move(file.toPath(), workingCopyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		file=null;
	}
	@Override
	public void close() {
		if(file!=null&&deleteFileOnClose)
		{
			file.delete();
		}
		file=null;
	}
	@Override
	public void streamTo(OutputStream outputStream) throws FileNotFoundException, IOException {
		byte[] buffer=new byte[StreamingHttpClient.bufferSize];
		try(FileInputStream fis=new FileInputStream(file))
		{
			int n;
			while((n=fis.read(buffer))>0)
			{
				outputStream.write(buffer, 0, n);
			}
		}
	}
	public QueryResponseFile setDeleteFileOnClose(boolean deleteFileOnClose) {
		this.deleteFileOnClose = deleteFileOnClose;
		return this;
	}
}
