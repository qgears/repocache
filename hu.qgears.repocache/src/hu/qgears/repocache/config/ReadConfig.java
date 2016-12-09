package hu.qgears.repocache.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import hu.qgears.repocache.CommandLineArgs;
import hu.qgears.repocache.p2plugin.P2RepoConfig;
import hu.qgears.repocache.p2plugin.P2RepoMode;

/**
 * Configuration parser for repocache implementation.
 */
public class ReadConfig {
	private static Log log=LogFactory.getLog(ReadConfig.class);

	private Map<String, String> mvnrepos = new HashMap<>();
	private Map<String, String> httprepos = new HashMap<>();
	private Map<String, P2RepoConfig> p2repos = new HashMap<>();
	private Map<String, ClientSetup> clients=Collections.synchronizedMap(new TreeMap<String, ClientSetup>());
	private CommandLineArgs args;
	private byte[] configXml;
	
	public ReadConfig (CommandLineArgs args) throws IOException {
		this.args=args;
		args.validate();
		parseConfig();
	}
		
	private void parseConfig() throws IOException {
		try {
			configXml=args.openConfigXml();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			try(InputStream configXMLInputStream=new ByteArrayInputStream(configXml))
			{
				Document doc = dBuilder.parse(configXMLInputStream);
				
				parseMavenRepos(doc);
				parseHttpRepos(doc);
				parseP2Repos(doc);
			}
		} catch (Exception e) {
			throw new IOException("Error reading configuration: "+args.config, e);
		}
	}

	private void parseMavenRepos (Document doc) {
		NodeList mavenRepos = doc.getElementsByTagName("mavenrepo");
		for (int temp = 0; temp < mavenRepos.getLength(); temp++) {
			Node mavenRepo = mavenRepos.item(temp);
			String repoName = DomParserUtil.getNodeAttr("name", mavenRepo);
			String remote = DomParserUtil.getNodeValue("remote", mavenRepo.getChildNodes());
			mvnrepos.put(repoName, remote);
			log.info("Maven repo name: " + repoName + ", remote: " + remote);
		}
	}
	
	private void parseHttpRepos (Document doc) {
		NodeList httpRepos = doc.getElementsByTagName("httprepo");
		for (int temp = 0; temp < httpRepos.getLength(); temp++) {
			Node httpRepo = httpRepos.item(temp);
			String repoName = DomParserUtil.getNodeAttr("name", httpRepo);
			String remote = DomParserUtil.getNodeValue("remote", httpRepo.getChildNodes());
			httprepos.put(repoName, remote);
			log.info("Http repo name: " + repoName + ", remote: " + remote);
		}
	}
	
	private void parseP2Repos (Document doc) {
		NodeList p2Repos = doc.getElementsByTagName("p2repo");
		for (int temp = 0; temp < p2Repos.getLength(); temp++) {
			Node p2Repo = p2Repos.item(temp);
			String repoName = DomParserUtil.getNodeAttr("name", p2Repo);
			String repoDesc = DomParserUtil.getNodeValue("desc", p2Repo.getChildNodes());
			String file = DomParserUtil.getNodeValue("file", p2Repo.getChildNodes());
			String primaryHost = DomParserUtil.getNodeValue("primaryHost", p2Repo.getChildNodes());
			String selectedMirror = DomParserUtil.getNodeValue("selectedMirror", p2Repo.getChildNodes());
			P2RepoMode repoMode = P2RepoMode.parse(DomParserUtil.getNodeValue("mode", p2Repo.getChildNodes()));
			p2repos.put(repoName, new P2RepoConfig(file, primaryHost, selectedMirror, repoMode, repoDesc));
			log.info("P2 repo name: " + repoName + ", file: " + file + ", primaryHost: " + primaryHost + ", selectedMirror: " + selectedMirror);
		}
	}
	
	public Map<String, String> getMvnrepos() {
		return mvnrepos;
	}

	public Map<String, String> getHttprepos() {
		return httprepos;
	}

	public Map<String, P2RepoConfig> getP2repos() {
		return p2repos;
	}
	
	public List<String> getAllRepos() {
		List<String> allRepos = new ArrayList<>();
		allRepos.addAll(mvnrepos.keySet());
		allRepos.addAll(httprepos.keySet());
		allRepos.addAll(p2repos.keySet());
		if (args.proxyPort != null) {
			allRepos.add("proxy-repo");
		}
		Collections.sort(allRepos);
		return allRepos;
	}
	
	public File getLocalGitRepo() {
		return args.repo;
	}

	public CommandLineArgs getCommandLine() {
		return args;
	}

	public byte[] getConfigXml() {
		return configXml;
	}

	public Map<String, ClientSetup> getClients() {
		return clients;
	}

	public ClientSetup getClientSetup(String clientIdentifier) {
		ClientSetup ret=clients.get(clientIdentifier);
		if(ret==null)
		{
			ret=new ClientSetup(clientIdentifier);
			clients.put(ret.getId(), ret);
		}
		return ret;
	}

}
