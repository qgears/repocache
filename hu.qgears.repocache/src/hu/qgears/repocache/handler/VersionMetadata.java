package hu.qgears.repocache.handler;

import java.util.Properties;

/**
 * Reads version metadata from version.txt, which is generated during maven build.
 * This data is shown in the footer of the status page (powered by...).
 * <p>
 * If the version.txt is missing, then hard coded defaults will be returned.
 * 
 * @author agostoni
 *
 */
public class VersionMetadata {

	private String gitHubUrl = "https://github.com/qgears/repocache";
	private String version = "2.0.0";
	
	public static VersionMetadata get(){
		VersionMetadata m = new VersionMetadata();
		Properties p = new Properties();
		try {
			p.load(VersionMetadata.class.getResourceAsStream("version.txt"));
			boolean taggedVersion = Boolean.valueOf(p.getProperty("tagged_version", "false"));
			m.version = p.getProperty("qualifiedVersion","2.0.0");  
			if (taggedVersion ) {
				m.gitHubUrl += "/tree/v"+m.version;
			}
		} catch (Exception e){
			//return hard-coded default metadata
		}
		return m;
	}
	
	public String getGitHubUrl() {
		return gitHubUrl;
	}
	
	public String getVersion() {
		return version;
	}
}
