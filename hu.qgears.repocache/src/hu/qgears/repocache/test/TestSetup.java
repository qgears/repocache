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
		clargs.repo=new File("/tmp/repo2");
		clargs.downloadsFolder=new File("/tmp/repoDownloads");
		clargs.setConfigOverride(UtilFile.loadFile(TestSetup.class.getResource("repos.xml")));
		clargs.setRepoModeConfigOverride(UtilFile.loadFile(new File("/home/akos/Downloads/repomodes.xml")), "/home/akos/Downloads/repomodes.xml");
		ReadConfig conf=new ReadConfig(clargs);
		RepoModeHandler hand=new RepoModeHandler(clargs);
		new RepoCache(conf, hand).start();
	}
}
