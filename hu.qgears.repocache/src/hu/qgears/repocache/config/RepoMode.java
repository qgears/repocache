package hu.qgears.repocache.config;

/**
 * Mode of operation for a given remote repository.
 * 
 * @author akosch
 */
public enum RepoMode {
	/**
	 * All requests to a remote repository will be served from the local cache.
	 * This is the default operation mode, and this mode is applied to all 
	 * remote repositories, even if none is specified in the textual access mode
	 * configuration file, {@value RepoConfiguration#ACCESS_RULE_CONFIG_FILE}.
	 */
	READ_ONLY,
	/**
	 * In this mode, artifacts not existing in the local cache, will be 
	 * downloaded from the remote repositoriy and will be offered for the user
	 * for commit. However, if an artifact exists in the cache, it will be 
	 * served from there, and the remote repository will not be queried for a
	 * current version.  
	 */
	ADD_ONLY,
	/**
	 * The local cache of a remote repository will be updated, if this mode is 
	 * specified for it. 
	 * Artifacts will be downloaded from the remote repository regardless they
	 * exist or not in the local storage and will be offered for the user to 
	 * commit. This way, locally cached artifacts can be updated with more recent 
	 * ones and new artifacts can be added, too.      
	 */
	UPDATE,
	/**
	 * In this mode, all artifacts will be served from the remote repository.
	 * The local cache will be ignored, and no newer artifacts will be offered
	 * for commit to update the local cache.  
	 */
	NO_CACHE_TRANSPARENT;
}
