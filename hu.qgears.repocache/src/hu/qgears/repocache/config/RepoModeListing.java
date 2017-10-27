package hu.qgears.repocache.config;

import java.util.List;

import hu.qgears.repocache.AbstractHTMLPage;
import hu.qgears.repocache.ClientQuery;

public class RepoModeListing extends AbstractHTMLPage {
	
	public RepoModeListing(ClientQuery query) {
		super(query);
	}
	
	@Override
	protected String getTitleFragment() {
		return "Repo mode config";
	}
	
	@Override
	protected void writeHTMLBody() {
		folder=true;
		write("<script language=\"javascript\" type=\"text/javascript\">\nfunction addParameter(link, param){\n    link += param;\n    return link;\n}\n</script>\n<h1>Repo Mode configuration</h1>\n<a href=\"../\">../</a><br/>\n<a href=\"./\">reload</a><br/>\n<h3>Repo mode setup</h3><br/>\nUpdated mode: <select name=\"modename\" id=\"modeid\" value=\"READ_ONLY\">\n  <option>READ_ONLY</option>\n  <option>ADD_ONLY</option>\n  <option>UPDATE</option>\n  <option>NO_CACHE_TRANSPARENT</option>\n</select><br/><br/>\n");
		List<String> allRepos = getQuery().rc.getConfiguration().getAllRepos();
		for (String repoName : allRepos) {
			RepoMode mode = getQuery().rc.getRepoModeHandler().getRepoMode(repoName);
			write("<strong>");
			writeObject(repoName);
			write("</strong> : ");
			writeObject(mode);
			write(" : <a href=\"setRepoMode?repoName=");
			writeObject(repoName);
			write("&amp;mode=\" onclick=\"location.href=addParameter(this.href,document.getElementById('modeid').value);return false;\">Update mode</a><br/><br/>\n");
		}
	}
	
}
