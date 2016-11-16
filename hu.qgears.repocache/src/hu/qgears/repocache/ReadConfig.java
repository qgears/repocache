package hu.qgears.repocache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadConfig {

	private static ReadConfig instance = null;
	
	private Map<String, String> mvnrepos = new HashMap<>();
	private Map<String, String> httprepos = new HashMap<>();
	private Map<String, RepoConfig> p2repos = new HashMap<>();
	
	private ReadConfig () {
		this.parseConfig();
	}
	
	public static ReadConfig getInstance() {
		if (instance == null) {
			instance = new ReadConfig();
		}
		return instance;
	}
	
	private void parseConfig() {
		File fXmlFile = new File("repos.xml");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
		
			parseMavenRepos(doc);
			parseHttpRepos(doc);
			parseP2Repos(doc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
			p2repos.put(repoName, new RepoConfig(file, primaryHost, selectedMirror));
			System.out.println("P2 repo name: " + repoName + ", file: " + file + ", primaryHost: " + primaryHost + ", selectedMirror: " + selectedMirror);
		}
	}
	
	public Map<String, String> getMvnrepos() {
		return mvnrepos;
	}

	public Map<String, String> getHttprepos() {
		return httprepos;
	}

	public Map<String, RepoConfig> getP2repos() {
		return p2repos;
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
	
}
