package hu.qgears.repocache.p2plugin;

/**
 * Configuration of a {@link P2CompositeArtifacts} repository to be cahced.
 */
public class P2RepoConfig {
	/**
	 * See: http://www.eclipse.org/downloads/download.php?file=/technology/epp/packages/neon/&protocol=http&format=xml
	 */
	//private String mirrorQuery="http://www.eclipse.org/downloads/download.php";
	/**
	 * Path of the repo within the downlaod server.
	 * Eg: "technology/epp/packages/neon"
	 */
	private String file;
	/**
	 * Primary host of the repo.
	 * Eg: "http://download.eclipse.org/"
	 */
	private String primaryHost;
	/**
	 * The mirror to use to download data.
	 * Eg. "http://mirror.tspu.ru/eclipse/"
	 */
	private String selectedMirror;
	private P2RepoMode repoMode;
	private String repoDesc;
	public P2RepoConfig(String file, String primaryHost, String selectedMirror, P2RepoMode repoMode, String repoDesc) {
		super();
		this.file = file;
		this.primaryHost = primaryHost;
		this.selectedMirror = selectedMirror;
		this.repoMode=repoMode;
		this.repoDesc=repoDesc;
	}
	public String getBaseUrl() {
		return selectedMirror+file+"/";
	}
	public String getPrimaryHost() {
		return primaryHost;
	}
	public String getFile() {
		return file;
	}
	public P2RepoMode getRepoMode() {
		return repoMode;
	}
	public String getRepoDesc() {
		return repoDesc;
	}
	
}
