package hu.qgears.repocache.config;

import java.util.List;

import hu.qgears.repocache.AbstractPage;
import hu.qgears.repocache.ClientQuery;

public class RepoModeListing extends AbstractPage {
	
	public RepoModeListing(ClientQuery query) {
		super(query);
	}
	
	@Override
	protected void doGenerate() {
		folder=true;
		write("<h1>Repo Mode configuration</h1>\n<a href=\"../\">../</a><br/>\n<a href=\"./\">reload</a><br/>\n");
		List<String> allRepos = getQuery().rc.getConfiguration().getAllRepos();
		for (String repoName : allRepos) {
			RepoMode mode = getQuery().rc.getRepoModeHandler().getRepoMode(repoName);
			write("Repo name: ");
			writeObject(repoName);
			write(" : mode=");
			writeObject(mode);
			write("<br/>\n");
		}
		getQuery().rc.getRepoModeHandler().saveRepoModeToXml();
	}
	
}
