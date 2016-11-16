package hu.qgears.repocache;

public class RepoConfig {
	/**
	 * See: http://www.eclipse.org/downloads/download.php?file=/technology/epp/packages/neon/&protocol=http&format=xml
	 */
	private String mirrorQuery="http://www.eclipse.org/downloads/download.php";
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
	public RepoConfig(String file, String primaryHost, String selectedMirror) {
		super();
		this.file = file;
		this.primaryHost = primaryHost;
		this.selectedMirror = selectedMirror;
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
}
