package hu.qgears.repocache.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import hu.qgears.repocache.CommandLineArgs;

public class RepoModeHandler {
	private static Log log=LogFactory.getLog(RepoModeHandler.class);
	
	private CommandLineArgs args;
	private byte[] repoModeConfigXml;
	
	private HashMap<String, RepoMode> repoModes = new HashMap<String, RepoMode>();
	
	public RepoModeHandler (CommandLineArgs args) throws IOException {
		this.args=args;
		args.validate();
		parseConfig();
	}
		
	private void parseConfig() throws IOException {
		try {
			repoModeConfigXml=args.openRepoModeConfigXml();
			if (repoModeConfigXml != null) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				try(InputStream configXMLInputStream=new ByteArrayInputStream(repoModeConfigXml))
				{
					Document doc = dBuilder.parse(configXMLInputStream);
					parseRepoModes(doc);
				}
			}
		} catch (Exception e) {
			throw new IOException("Error reading configuration: "+args.config, e);
		}
	}
	
	private void parseRepoModes (Document doc) {
		NodeList repos = doc.getElementsByTagName("repo");
		for (int temp = 0; temp < repos.getLength(); temp++) {
			Node repo = repos.item(temp);
			String repoName = DomParserUtil.getNodeAttr("name", repo);
			String mode = DomParserUtil.getNodeValue("mode", repo.getChildNodes());
			repoModes.put(repoName, RepoMode.parse(mode));
			log.info("Maven repo initial mode, name: " + repoName + ", mode: " + mode);
		}
	}
	
	public RepoMode getRepoMode(String repoName) {
		if (repoModes.containsKey(repoName)) {
			return repoModes.get(repoName);
		} else {
			repoModes.put(repoName, RepoMode.READ_ONLY);
			return RepoMode.READ_ONLY;
		}
	}

	public void setRepoMode(String repoName, RepoMode mode) {
		repoModes.put(repoName, mode);
		saveRepoModeToXml();
	}
	
	public boolean isRepoUpdatable(String repoName) {
		RepoMode mode = getRepoMode(repoName);
		return RepoMode.UPDATE.equals(mode);
	}
	
	public boolean isRepoAddable(String repoName) {
		RepoMode mode = getRepoMode(repoName);
		return RepoMode.UPDATE.equals(mode) || RepoMode.ADD_ONLY.equals(mode);
	}
	
	public boolean isRepoTransparent(String repoName) {
		RepoMode mode = getRepoMode(repoName);
		return RepoMode.NO_CACHE_TRANSPARENT.equals(mode);
	}
	
	public HashMap<String, RepoMode> getRepoModes() {
		return repoModes;
	}
	
	private void saveRepoModeToXml() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("repos");
			doc.appendChild(rootElement);
			
			List<String> repos = new ArrayList<String>(repoModes.keySet());
			Collections.sort(repos);
			for (String repoName : repos) {
				String modeStr = repoModes.get(repoName).name();
				Element repo = doc.createElement("repo");
				rootElement.appendChild(repo);

				// set attribute to repo element
				Attr attr = doc.createAttribute("name");
				attr.setValue(repoName);
				repo.setAttributeNode(attr);
				
				// mode element
				Element mode = doc.createElement("mode");
				mode.appendChild(doc.createTextNode(modeStr));
				repo.appendChild(mode);
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(args.repoModeConfig);

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

			log.info("Repo mode configuration file updated. path: " + args.repoModeConfig.getAbsolutePath());
		} catch (DOMException e) {
			log.error("Error saving repo mode configuration.", e);
		} catch (ParserConfigurationException e) {
			log.error("Error saving repo mode configuration.", e);
		} catch (TransformerException e) {
			log.error("Error saving repo mode configuration.", e);
		}		
	}
	
}
