package hu.qgears.repocache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configuration parser for repocache implementation.
 */
public class ReadConfig {

	private Map<String, String> mvnrepos = new HashMap<>();
	private Map<String, String> httprepos = new HashMap<>();
	private Map<String, P2RepoConfig> p2repos = new HashMap<>();
	private Map<String, ClientSetup> clients=Collections.synchronizedMap(new TreeMap<>());
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
				parseLocalGitRepo(doc);
			}
		} catch (Exception e) {
			throw new IOException("Error reading configuration: "+args.config, e);
		}
	}

	private void parseLocalGitRepo (Document doc) {
		NodeList localRepos = doc.getElementsByTagName("localgitrepo");
		for (int temp = 0; temp < localRepos.getLength(); temp++) {
			Node localRepo = localRepos.item(temp);
			String path = getNodeValue("path", localRepo.getChildNodes());
		}
	}
	
	private void parseMavenRepos (Document doc) {
		NodeList mavenRepos = doc.getElementsByTagName("mavenrepo");
		for (int temp = 0; temp < mavenRepos.getLength(); temp++) {
			Node mavenRepo = mavenRepos.item(temp);
			String repoName = getNodeAttr("name", mavenRepo);
			String remote = getNodeValue("remote", mavenRepo.getChildNodes());
			mvnrepos.put(repoName, remote);
			System.out.println("Maven repo name: " + repoName + ", remote: " + remote);
		}
	}
	
	private void parseHttpRepos (Document doc) {
		NodeList httpRepos = doc.getElementsByTagName("httprepo");
		for (int temp = 0; temp < httpRepos.getLength(); temp++) {
			Node httpRepo = httpRepos.item(temp);
			String repoName = getNodeAttr("name", httpRepo);
			String remote = getNodeValue("remote", httpRepo.getChildNodes());
			httprepos.put(repoName, remote);
			System.out.println("Http repo name: " + repoName + ", remote: " + remote);
		}
	}
	
	private void parseP2Repos (Document doc) {
		NodeList p2Repos = doc.getElementsByTagName("p2repo");
		for (int temp = 0; temp < p2Repos.getLength(); temp++) {
			Node p2Repo = p2Repos.item(temp);
			String repoName = getNodeAttr("name", p2Repo);
			String file = getNodeValue("file", p2Repo.getChildNodes());
			String primaryHost = getNodeValue("primaryHost", p2Repo.getChildNodes());
			String selectedMirror = getNodeValue("selectedMirror", p2Repo.getChildNodes());
			P2RepoMode repoMode = P2RepoMode.parse(getNodeValue("mode", p2Repo.getChildNodes()));
			p2repos.put(repoName, new P2RepoConfig(file, primaryHost, selectedMirror, repoMode));
			System.out.println("P2 repo name: " + repoName + ", file: " + file + ", primaryHost: " + primaryHost + ", selectedMirror: " + selectedMirror);
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
	
	public File getLocalGitRepo() {
		return args.repo;
	}

	private String getNodeAttr(String attrName, Node node ) {
	    NamedNodeMap attrs = node.getAttributes();
	    for (int y = 0; y < attrs.getLength(); y++ ) {
	        Node attr = attrs.item(y);
	        if (attr.getNodeName().equalsIgnoreCase(attrName)) {
	            return attr.getNodeValue();
	        }
	    }
	    return "";
	}
	
	protected String getNodeValue(String tagName, NodeList nodes ) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.TEXT_NODE )
	                    return data.getNodeValue();
	            }
	        }
	    }
	    return "";
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
			ret=new ClientSetup();
		}
		return ret;
	}

	public void setClientConfiguration(ClientSetup cs) {
		if(cs.getMode()==EClientMode.normal)
		{
			clients.remove(cs.getId());
		}else
		{
			clients.put(cs.getId(), cs);
		}
	}
}
