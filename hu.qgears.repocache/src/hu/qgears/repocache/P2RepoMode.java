package hu.qgears.repocache;

public enum P2RepoMode {
	/**
	 * Normal replication of the P2 repository.
	 */
	normal,
	/**
	 * Replace semantics of P2 repository: new versions of the listings are always replaced and old versions are not kept
	 * by the original source. The cache has to keep all versions. 
	 */
	replace;

	public static P2RepoMode parse(String nodeValue) {
		if(nodeValue==null||nodeValue.length()==0)
		{
			return normal;
		}
		return P2RepoMode.valueOf(nodeValue);
	}
	
}
