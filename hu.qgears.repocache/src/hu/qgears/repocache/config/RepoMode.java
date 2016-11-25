package hu.qgears.repocache.config;

public enum RepoMode {
	READ_ONLY,
	ADD_ONLY,
	UPDATE,
	NO_CACHE_TRANSPARENT;
	
	public static RepoMode parse(String nodeValue) {
		if(nodeValue==null||nodeValue.length()==0)
		{
			return READ_ONLY;
		}
		return RepoMode.valueOf(nodeValue);
	}

}
