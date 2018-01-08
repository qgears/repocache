package hu.qgears.repocache.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpSession;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.repocache.AbstractHTMLPage;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientQueryHttp;
import hu.qgears.repocache.qpage.QButton;
import hu.qgears.repocache.qpage.QPage;
import hu.qgears.repocache.qpage.QPageManager;
import hu.qgears.repocache.qpage.QTextEditor;

public class ConfigListing extends AbstractHTMLPage {
	private QPage page;
//	private QTextEditor editorConfigXml;
//	private QButton button;
	public ConfigListing(ClientQuery query) {
		super(query);
	}
	
	@Override
	protected String getTitleFragment() {
		return "Config";
	}
	@Override
	protected void writeHtmlHeaders() {
		super.writeHtmlHeaders();
		page.writeHeaders(this);
	}
	protected ClientQueryHttp getHttpQuery()
	{
		return (ClientQueryHttp)getQuery();
	}
	@Override
	protected void doGenerate() throws IOException {
		HttpSession sess=getHttpQuery().baseRequest.getSession();
		QPageManager qpm=QPageManager.getManager(sess);
		page=qpm.getPage(getHttpQuery().baseRequest);
		if(page!=null)
		{
			page.handle(this, getHttpQuery().getInMemoryPost());
			return;
		}else
		{
			page=new QPage(qpm);
			QTextEditor editorConfigXml=new QTextEditor(page, "config.xml");
			editorConfigXml.text.setProperty(new String(getHttpQuery().rc.getConfiguration().getConfigXml(), StandardCharsets.UTF_8));
			editorConfigXml.text.getPropertyChangedEvent().addListener(new UtilEventListener<String>() {
				
				@Override
				public void eventHappened(String msg) {
					System.out.println("Text: "+msg);
				}
			});
			QButton button=new QButton(page, "submit-config.xml");
			button.clicked.addListener(new UtilEventListener<QButton>() {
				@Override
				public void eventHappened(QButton msg) {
					System.out.println("Button clicked!");
				}
			});
		}
		super.doGenerate();
	}
	@Override
	protected void writeHTMLBody() {
		printParameters();
		write("<script language=\"javascript\" type=\"text/javascript\">\nfunction addParameter(link, param){\n    link += param;\n    return link;\n}\n</script>\n\n<h1>Repo Cache configuration</h1>\n<a href=\"../\">../</a><br/>\n<a href=\"./\">reload</a><br/>\nLocal Only: ");
		writeObject(getQuery().rc.getConfiguration().getCommandLine().localOnly);
		write("<br/>\n<a href=\"config.xml\">config.xml</a><br/>\n<textarea id=\"config.xml\" rows=\"4\" cols=\"50\"></textarea>\n<button id=\"submit-config.xml\">Submit config.xml</button>\n<br/>\n<a href=\"repoModeConfig\\\">repoModeConfig</a><br/>\nClient IP address: ");
		writeObject(getQuery().getClientIdentifier());
		write("<br/>\n<h2>Current client setup</h2>\nUpdater clients, validity in minutes: <input id=\"validity\" name=\"validity\"/><br/>\n");
		for(EClientMode m: EClientMode.values())
		{
			write("<a href=\"setClientMode?client=");
			writeObject(getQuery().getClientIdentifier());
			write("&amp;mode=");
			writeObject(m);
			write("&amp;validInMinute=\" onclick=\"location.href=addParameter(this.href,document.getElementById('validity').value);return false;\">Set ");
			writeObject(getQuery().getClientIdentifier());
			write(" client to ");
			writeObject(m);
			write("</a><br/>\n");
		}
		if (!getQuery().rc.getConfiguration().getClientSetup(getQuery().getClientIdentifier()).isShawRealFolderListing()) {
			write("<a href=\"setClientMode?client=");
			writeObject(getQuery().getClientIdentifier());
			write("&amp;shawRealFolderListing=true\">Set ");
			writeObject(getQuery().getClientIdentifier());
			write(" client ENABLE real folder listing.</a><br/>\n");
		} else {
			write("<a href=\"setClientMode?client=");
			writeObject(getQuery().getClientIdentifier());
			write("&amp;shawRealFolderListing=false\">Set ");
			writeObject(getQuery().getClientIdentifier());
			write(" client DISABLE real folder listing.</a><br/>\n");
		}
		for(EClientMode m: EClientMode.values())
		{
			write("<a href=\"setClientMode?client=this&amp;mode=");
			writeObject(m);
			write("&amp;validInMinute=\" onclick=\"location.href=addParameter(this.href,document.getElementById('validity').value);return false;\">Set this client to ");
			writeObject(m);
			write("</a><br/>\n");
		}
		if (!getQuery().rc.getConfiguration().getClientSetup(getQuery().getClientIdentifier()).isShawRealFolderListing()) {
			write("<a href=\"setClientMode?client=this&amp;shawRealFolderListing=true\">Set this client ENABLE real folder listing.</a><br/>\n");
		} else {
			write("<a href=\"setClientMode?client=this&amp;shawRealFolderListing=false\">Set this client DISABLE real folder listing.</a><br/>\n");
		}
		write("<ul>\n");
		synchronized (getQuery().rc.getConfiguration().getClients()) {
			for(ClientSetup c: getQuery().rc.getConfiguration().getClients().values())
			{
				write("<li>");
				writeObject(c);
				write("</li>\n");
			}
		}
		write("</ul>\n<h2>Current staging area</h2>\n(Added but not committed changes.)\n<a href=\"commit\">Execute commit now</a>\n<a href=\"revert\">Execute revert</a>\n<pre>\n");
		writeHtml(getQuery().rc.getCommitTimer().getCurrentStagingMessage());
		write("\n</pre>\n");
	}
}
