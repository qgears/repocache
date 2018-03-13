package hu.qgears.repocache.ssh;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilProcess;

public class InitCertsFolder {
	static Log log=LogFactory.getLog(InitCertsFolder.class);
	public String init(File certsFolder) {
		try {
			installFile(certsFolder, "cert-config.txt", false);
			installFile(certsFolder, "dynamiccert.sh", true);
			installFile(certsFolder, "rootcerts.sh", true);
			installFile(certsFolder, "template.cert.config", true);
			
			if(!new File(certsFolder, "public/repocache.qgears.com.crt").exists()
			|| !new File(certsFolder, "keys/repocache.qgears.com.private").exists())
			{
				File script=new File(certsFolder, "rootcerts.sh");
				ProcessBuilder pb=new ProcessBuilder(new String[]{script.getAbsolutePath()}).directory(certsFolder);
				Process p=pb.start();
				Pair<byte[],byte[]> out=UtilProcess.saveOutputsOfProcess(p).get();
				return "New key created: out: "+new String(out.getA(), StandardCharsets.UTF_8)+" Err: "+new String(out.getB(), StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			log.error("Initializeing certs", e);
			return "error (see logs): "+e.getMessage();
		}
		return "Keys already existed";
	}

	private void installFile(File certsFolder, String string, boolean exec) throws IOException {
		File f=new File(certsFolder, string);
		if(!f.exists())
		{
			f.getParentFile().mkdirs();
			byte[] content=UtilFile.loadFile(getClass().getResource("certs/"+string));
			UtilFile.saveAsFile(f, content);
			if(exec)
			{
				f.setExecutable(true);
			}
		}
	}

}
