package hu.qgears.repocache.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.repocache.AbstractHTMLPage;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientQueryHttp;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.qpage.QButton;
import hu.qgears.repocache.qpage.QPage;
import hu.qgears.repocache.qpage.QPageManager;
import hu.qgears.repocache.qpage.QTextEditor;

public class ConfigHandler2 extends AbstractHTMLPage
{
	private static Log log=LogFactory.getLog(ConfigHandler2.class);
	private QPage page;
	public ConfigHandler2(ClientQuery query) {
		super(query);
	}
	protected ClientQueryHttp getHttpQuery()
	{
		return (ClientQueryHttp)getQuery();
	}
	
	@Override
	protected void writeHtmlHeaders() {
		super.writeHtmlHeaders();
		page.writeHeaders(this);
		write("<script language=\"javascript\" type=\"text/javascript\">\nfunction toggleVisible(id)\n{\n\tvar x = document.getElementById(id);\n    if (x.style.display === \"none\") {\n        x.style.display = \"block\";\n    } else {\n        x.style.display = \"none\";\n    }\n}\n</script>\n");
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
			QTextEditor editorAliases=new QTextEditor(page, "aliases");
			editorAliases.text.setProperty(new String(getHttpQuery().rc.getConfiguration().getConfigXml(), StandardCharsets.UTF_8));
			editorAliases.text.getPropertyChangedEvent().addListener(new UtilEventListener<String>() {
				@Override
				public void eventHappened(String msg) {
					System.out.println("Text: "+msg);
				}
			});
			QButton button=new QButton(page, "submit-aliases");
			button.clicked.addListener(new UtilEventListener<QButton>() {
				@Override
				public void eventHappened(QButton msg) {
					System.out.println("Button clicked!");
				}
			});
			QTextEditor editorMaven=new QTextEditor(page, "maven");
			QButton buttonMaven=new QButton(page, "submit-maven");
			final QTextEditor accessRules=new QTextEditor(page, "access");
			accessRules.text.setProperty(getHttpQuery().rc.getAccessRules().getConfig());
			QButton buttonAccess=new QButton(page, "submit-access");
			buttonAccess.clicked.addListener(new UtilEventListener<QButton>() {
				@Override
				public void eventHappened(QButton msg) {
					getHttpQuery().rc.getAccessRules().setConfig(accessRules.text.getProperty());
				}
			});
			QButton buttonAccessReload=new QButton(page, "reload-access");
			buttonAccessReload.clicked.addListener(new UtilEventListener<QButton>() {
				@Override
				public void eventHappened(QButton msg) {
					accessRules.text.setProperty(getHttpQuery().rc.getAccessRules().getConfig());
				}
			});
		}
		super.doGenerate();
	}
	@Override
	protected void writeHTMLBody() {
		write("<h1>Repo Cache configuration2</h1>\n<a href=\"../\">Repo cache root folder (../)</a><br/>\n\n\n\n\n<h2>Access rules</h2>\n\n<button onclick=\"toggleVisible('help-access')\">toggle help</button><br/>\n<div id=\"help-access\" style=\"display: none;\">\n<p>Access rules define how the content is accessed:</p>\n<ul>\n<li>ro: read only. Content is read from the repo cache. Internet is never accessed.</li>\n<li>add: add only. Content is read from the repo cache. In case the resource does not exist then the Internet is accessed. The returned value is put into the cache.</li>\n<li>update: add and update. Content is read from Internet. If it has been changed online then it is updated in the repo cache.\n<li>transparent: Content is read from the Internet and returned to the query. Cache is not read and is not updated at all.</li>\n</ul>\n<p>Access rules are defined on the in repo path not on the real world path.\nThis means that in case an alias is specified for the path /proxy/http/clientqueries.com/ to\n/proxy/http/repocachedownloadsinstead.com/ then the access rules are queried for /proxy/http/clientqueries.com/.</p> \n<p>The first matching line determines what happens to a query url. No matching line means read only.</p> \nExample:\n<pre>\nupdate /proxy/https/qgears.com/\nro /proxy/https/ubuntu.com/\ntransparent /proxy/https/weather.com/\nadd /proxy/http/newimageeveryday.com/\n</pre>\n</div>\n\n<textarea id=\"access\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit-access\">Submit access rules</button> <button id=\"reload-access\">Reload access rules</button>\n<br/>\n\n\n<div style=\"display: none;\">\n\nThese objects are not finished so they are hidden in browser\n\n<h2>Target Aliases</h2>\n\n<button onclick=\"toggleVisible('help-alias')\">toggle help</button><br/>\n<div id=\"help-alias\" style=\"display: none;\">\n<p>Aliases can be used to transform a repo cache path to turn to a different real world URL. Two paths separated by a space is a valid alias entry.\nLines starting with # are comments.</p>\n\n<p>The path specified are paths within the repo cache. So it is possible to even change the protocol of the access.</p> \n\n<p>Using aliases it is possible to use a specific mirror of a project.</p>\n\nFormat example to connect a private mirror instead of the original:\n<pre>\n/proxy/https/eclipse.org/ /proxy/https/qgears.com/private-eclipse-mirror/\n</pre>\n</div>\n<textarea id=\"aliases\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit-aliases\">Submit target aliases</button>\n<br/>\n<h2>maven repos</h2>\n\n<p>Maven repos are configured by a name and a remote url. After configuration the remote url will be accessible through the /maven/{name}/ path in the repo cache.</p>\n\nFormat example:\n<pre>\nmavencentral https://repo.maven.apache.org/maven2/\n</pre>\n<textarea id=\"maven\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit-maven\">Submit maven repos</button>\n<br/>\n\n<h2>P2 repositories</h2>\n\nFormat:\n<pre>repoName file primaryHost selectedMirror repoMode repoDesc</pre>\n</div>\n");
	}


	@Override
	protected String getTitleFragment() {
		return "Config2";
	}

	public void handle() throws IOException {
		QueryResponse resp=generate();
		resp.contentType="text/html; utf-8";
		getQuery().reply(resp);
	}
	
}
