package hu.qgears.repocache.p2plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.qgears.repocache.Path;

public class P2VersionFolderUtil {
	
	private static P2VersionFolderUtil instance = null;
	private static String localRepo = null;
	
	public P2VersionFolderUtil(String localRepo) {
		P2VersionFolderUtil.localRepo = localRepo;
		P2VersionFolderUtil.instance = this;
	}
	
	public static P2VersionFolderUtil getInstance() {
		return instance;
	}

	public List<String> listFolders (String p2Repo) {
		File repoFolder = this.getP2VersionFolder(p2Repo);
		List<String> folderList = new ArrayList<>();
		File[] files = repoFolder.listFiles();
		for (File dir : files) if (dir.isDirectory()) folderList.add(dir.getName());
		Collections.sort(folderList);
		return folderList;
	}
	
	private int getLastVersionUsed(File repoFolder) {
		int version = 0;
		if (repoFolder.exists() && repoFolder.isDirectory()) {
			for (File dir : repoFolder.listFiles()) {
				if (dir.isDirectory()) {
					try {
						int dirVersion = Integer.parseInt(dir.getName());
						if (dirVersion > version) version = dirVersion;
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return version;
	}
	
	public Path getLastVersionPath (String p2Repo) {
		Path p = new Path("p2/" + p2Repo);
		File repoFolder = this.getP2VersionFolder(p2Repo);
		p.add(""+getLastVersionUsed(repoFolder));
		return p;
	}
	
	public int createNextVersionFolder(String p2Repo) {
		File repoFolder = this.getP2VersionFolder(p2Repo);
		int lastVersion = getLastVersionUsed(repoFolder);
		File f2 = new File(repoFolder.getAbsolutePath() + "/" + (++lastVersion));
		f2.mkdir();
		return lastVersion;
	}

	private File getP2VersionFolder (String p2Repo) {
		File f = new File(P2VersionFolderUtil.localRepo + "/p2/" + p2Repo);
		return f;
	}
	
	public void createP2VersionFolderIfNotExist (String p2Repo) {
		File f = getP2VersionFolder(p2Repo);
		if (!f.exists()) {
			f.mkdir();
			createNextVersionFolder(p2Repo);
		}
	}
	
}
