package hu.qgears.repocache.test;

import java.io.File;

import hu.qgears.commons.UtilFile;
import hu.qgears.repocache.CommandLineArgs;
import hu.qgears.repocache.RepoCache;
import hu.qgears.repocache.config.ReadConfig;
import hu.qgears.repocache.config.RepoModeHandler;

public class TestSetup {
	public static void main(String[] args) throws Exception {
		CommandLineArgs clargs=new CommandLineArgs();
		clargs.repo=new File("/tmp/repo4");
		clargs.downloadsFolder=new File("/tmp/repoDownloads");
		clargs.setConfigOverride(UtilFile.loadFile(TestSetup.class.getResource("ubiRepos.xml")));
		byte[] repomodes = null;
		try {
			repomodes = UtilFile.loadFile(new File("/home/akos/Downloads/repomodes.xml"));
		} catch (Exception e) {
			System.out.println("Error loading repomodes file: " + e.getMessage());
		}
		clargs.setRepoModeConfigOverride(repomodes, "/home/akos/Downloads/repomodes.xml");
		clargs.proxyPort = 9000;
		ReadConfig conf=new ReadConfig(clargs);
		RepoModeHandler hand=new RepoModeHandler(clargs);
		new RepoCache(conf, hand).start();
	}
}
