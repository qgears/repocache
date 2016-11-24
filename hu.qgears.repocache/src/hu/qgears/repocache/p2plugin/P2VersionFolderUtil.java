package hu.qgears.repocache.p2plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.qgears.repocache.Path;

public class P2VersionFolderUtil {
	
	private static P2VersionFolderUtil instance = null;
	private static String localRepo = null;
	public static final String P2_PATH = "p2";
	
	public P2VersionFolderUtil(String localRepo) {
		P2VersionFolderUtil.localRepo = localRepo;
		P2VersionFolderUtil.instance = this;
	}
	
	public static P2VersionFolderUtil getInstance() {
		return instance;
	}

	public List<String> listFolders (String p2Repo) {
		File repoFolder = getP2VersionFolder(p2Repo);
		List<String> folderList = new ArrayList<>();
		File[] files = repoFolder.listFiles();
		for (File dir : files) if (dir.isDirectory()) folderList.add(dir.getName());
		Collections.sort(folderList);
		return folderList;
	}
	
	public int getLastVersionUsed(String p2Repo) {
		File repoFolder = getP2VersionFolder(p2Repo);
		return getLastVersionUsed(repoFolder);
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
		Path p = new Path(P2_PATH);
		p.add(p2Repo);
		File repoFolder = getP2VersionFolder(p2Repo);
		p.add(""+getLastVersionUsed(repoFolder));
		return p;
	}
	
	private File getP2VersionFolder (String p2Repo) {
		File f = new File("/" + new Path(P2VersionFolderUtil.localRepo).add(P2_PATH).add(p2Repo).toStringPath());
		return f;
	}
	
	public Path createP2VersionFolderIfNotExist (String p2Repo) {
		File f = getP2VersionFolder(p2Repo);
		if (!f.exists()) {
			f.mkdir();
			return new Path(P2_PATH).add(p2Repo);
		}
		return null;
	}
	
}
