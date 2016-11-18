package hu.qgears.repocache.test;

import java.io.File;

import hu.qgears.commons.UtilFile;
import hu.qgears.repocache.CommandLineArgs;
import hu.qgears.repocache.ReadConfig;
import hu.qgears.repocache.RepoCache;

public class TestSetup {
	public static void main(String[] args) throws Exception {
		CommandLineArgs clargs=new CommandLineArgs();
		clargs.repo=new File("/tmp/repo2");
		clargs.setConfigOverride(UtilFile.loadFile(TestSetup.class.getResource("repos.xml")));
		ReadConfig conf=new ReadConfig(clargs);
		new RepoCache(conf).start();
	}
}
