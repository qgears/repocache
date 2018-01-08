package hu.qgears.repocache.config;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.Slot;
import hu.qgears.repocache.AbstractHTMLPage;
import hu.qgears.repocache.ClientQuery;
import hu.qgears.repocache.ClientQueryHttp;
import hu.qgears.repocache.CommitTimer;
import hu.qgears.repocache.QueryResponse;
import hu.qgears.repocache.qpage.QButton;
import hu.qgears.repocache.qpage.QLabel;
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
			{
				final QTextEditor editor=new QTextEditor(page, "name");
				editor.text.setPropertyFromServer(getHttpQuery().rc.getAccessRules().getName());
				QButton submit=new QButton(page, "submit-name");
				submit.clicked.addListener(new UtilEventListener<QButton>() {
					@Override
					public void eventHappened(QButton msg) {
						try {
							getHttpQuery().rc.getAccessRules().saveName(editor.text.getProperty());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
			{
				final QTextEditor accessRules=new QTextEditor(page, "access");
				accessRules.text.setPropertyFromServer(getHttpQuery().rc.getAccessRules().getAccessRules());
				QButton buttonAccess=new QButton(page, "submit-access");
				buttonAccess.clicked.addListener(new UtilEventListener<QButton>() {
					@Override
					public void eventHappened(QButton msg) {
						try {
							getHttpQuery().rc.getAccessRules().saveAccessRules(accessRules.text.getProperty());
						} catch (IOException e) {
							// TODO Show problem to user!
							e.printStackTrace();
						}
					}
				});
				QButton buttonAccessReload=new QButton(page, "reload-access");
				buttonAccessReload.clicked.addListener(new UtilEventListener<QButton>() {
					@Override
					public void eventHappened(QButton msg) {
						accessRules.text.setPropertyFromServer(getHttpQuery().rc.getAccessRules().getAccessRules());
					}
				});
			}
			
			{
				final QTextEditor editorClientAlias=new QTextEditor(page, "client-alias");
				editorClientAlias.text.setPropertyFromServer(getHttpQuery().rc.getAccessRules().getClientAlias());
				QButton clientAliasButton=new QButton(page, "submit-client-alias");
				clientAliasButton.clicked.addListener(new UtilEventListener<QButton>() {
					@Override
					public void eventHappened(QButton msg) {
						try {
							getHttpQuery().rc.getAccessRules().saveClientAlias(editorClientAlias.text.getProperty());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
			
			{
				final QTextEditor editor=new QTextEditor(page, "plugins");
				editor.text.setPropertyFromServer(getHttpQuery().rc.getAccessRules().getPluginsConfig());
				QButton submit=new QButton(page, "submit-plugins");
				submit.clicked.addListener(new UtilEventListener<QButton>() {
					@Override
					public void eventHappened(QButton msg) {
						try {
							getHttpQuery().rc.getAccessRules().savePluginsConfig(editor.text.getProperty());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
			{
				QTextEditor editorInternetAlias=new QTextEditor(page, "internet-alias");
				editorInternetAlias.text.setPropertyFromServer(getHttpQuery().rc.getAccessRules().getInternetAliases());
				QButton submitInternetAlias=new QButton(page, "submit-internet-alias");
				submitInternetAlias.clicked.addListener(new UtilEventListener<QButton>() {
					@Override
					public void eventHappened(QButton msg) {
						System.out.println("Button clicked!");
					}
				});
			}
			{
				final QLabel staging=new QLabel(page, "staging");
				QButton commit=new QButton(page, "commit");
				QButton revert=new QButton(page, "revert");
				final CommitTimer ct=getQuery().rc.getCommitTimer();
				updateStagingOnUIThread(staging, ct);
				final UtilEventListener<CommitTimer> l=new UtilEventListener<CommitTimer>() {
					@Override
					public void eventHappened(CommitTimer msg) {
						updateStaging(staging, msg);
					}
				};
				ct.commitStateChanged.addListener(l);
				page.disposedEvent.addOnReadyHandler(new Slot<SignalFuture<QPage>>() {
					@Override
					public void signal(SignalFuture<QPage> value) {
						System.out.println("Config2 page disposed!!!");
						ct.commitStateChanged.removeListener(l);
					}
				});
				
				commit.clicked.addListener(new UtilEventListener<QButton>() {
					@Override
					public void eventHappened(QButton msg) {
							new Thread("Commit thread")
							{
								public void run() {
									try {
										ct.executeCommit();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								};
							}.start();
					}
				});
				revert.clicked.addListener(new UtilEventListener<QButton>() {
					@Override
					public void eventHappened(QButton msg) {
						new Thread("Revert thread")
						{
							public void run() {
								try {
									ct.executeRevert();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							};
						}.start();
					}
				});
			}
			{
				final QLabel configUpdated=new QLabel(page, "config-updated");
				configUpdated.innerhtml.setPropertyFromServer("");
				final AccessRules ar=getQuery().rc.getAccessRules();
				final UtilEventListener<AccessRules> changed=new UtilEventListener<AccessRules>() {
					@Override
					public void eventHappened(AccessRules msg) {
						if(!page.isThread())
						{
							page.submitToUI(new Runnable() {
								public void run() {
										configUpdated.innerhtml.setPropertyFromServer("Configuration updated from other client! Reload page!");
								}
							});
						}
					}
				};
				ar.configChanged.addListener(changed);
				page.disposedEvent.addOnReadyHandler(new Slot<SignalFuture<QPage>>() {
					@Override
					public void signal(SignalFuture<QPage> value) {
						ar.configChanged.removeListener(changed);
					}
				});
			}
		}
		super.doGenerate();
	}
	private void updateStagingOnUIThread(QLabel staging, CommitTimer msg)
	{
		StringWriter sw=new StringWriter();
		sw.write("<pre>");
		sw.write(getQuery().rc.getCommitTimer().getCurrentStagingMessage());
		sw.write("</pre>");
		staging.innerhtml.setPropertyFromServer(sw.toString());
	}
	protected void updateStaging(final QLabel staging, final CommitTimer msg) {
		staging.getPage().submitToUI(new Runnable() {
			
			@Override
			public void run() {
				updateStagingOnUIThread(staging, msg);
			}
		});
	}
	@Override
	protected void writeHTMLBody() {
		write("<h1>");
		writeHtml(getQuery().rc.getAccessRules().getName());
		write(" configuration</h1>\n<a href=\"../\">Repo cache root folder (../)</a><br/>\n\n<h2>Name</h2>\n\nName of the repository shown in configuration web pages.<br/>\n<input id=\"name\" size=\"100\"></input>\n<button id=\"submit-name\">Submit name</button>\n\n\n<div id=\"config-updated\"></div>\n<h2>Access rules</h2>\n\n<button onclick=\"toggleVisible('help-access')\">toggle help</button><br/>\n<div id=\"help-access\" style=\"display: none;\">\n<p>Access rules define how the content is accessed:</p>\n<ul>\n<li>ro: read only. Content is read from the repo cache. Internet is never accessed.</li>\n<li>add: add only. Content is read from the repo cache. In case the resource does not exist then the Internet is accessed. The returned value is put into the cache.</li>\n<li>update: add and update. Content is read from Internet. If it has been changed online then it is updated in the repo cache.\n<li>transparent: Content is read from the Internet and returned to the query. Cache is not read and is not updated at all.</li>\n</ul>\n<p>Access rules are defined on the in repo path not on the real world path.\nThis means that in case an alias is specified for the path /proxy/http/clientqueries.com/ to\n/proxy/http/repocachedownloadsinstead.com/ then the access rules are queried for /proxy/http/clientqueries.com/.</p> \n<p>The first matching line determines what happens to a query url. No matching line means read only.</p> \nExample:\n<pre>\nupdate /proxy/https/qgears.com/\nro /proxy/https/ubuntu.com/\ntransparent /proxy/https/weather.com/\nadd /proxy/http/newimageeveryday.com/\n</pre>\n</div>\n\n<textarea id=\"access\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit-access\">Submit access rules</button>\n<span id=\"hide-reload-access\" style=\"display: none;\"><button id=\"reload-access\">Reload Access ruels</button></span>\n<br/>\n\n\n<h2>Client side aliases</h2>\n\n<button onclick=\"toggleVisible('help-client-alias')\">toggle help</button><br/>\n<div id=\"help-client-alias\" style=\"display: none;\">\nClient side aliases can be used to access the same resource on multiple URLs. For example:\n\n<pre>/proxy/http/192.168.1.24/ /proxy/http/my.local.servers.global.name/</pre>\n\nIn this case each time the /proxy/http/192.168.1.24/ server is queried the content from /proxy/http/my.local.servers.global.name/ will be replied.  \n</div>\n\n<textarea id=\"client-alias\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit-client-alias\">Submit client alias</button> <span id=\"hide-reload-client-alias\" style=\"display: none;\"><button id=\"reload-client-alias\">Reload client alias</button></span>\n<br/>\n\n<h2>Plugins</h2>\n\n<button onclick=\"toggleVisible('help-plugins')\">toggle help</button><br/>\n<div id=\"help-plugins\" style=\"display: none;\">\nPlugins can be used to add special abilities to repo cache folders.\n\nFormat: {plugind-id} {path} {additional parameters}. Lines starting with # are comments.\n\nExample:\n\n<pre>replace-p2 /proxy/http/path-of-replace-mode-p2/</pre>\n\nIn this case each time the /proxy/http/path-of-replace-mode-p2/ internet server is accessed then the replace-p2 plugin is used to query content.  \n</div>\n\n<textarea id=\"plugins\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit-plugins\">Submit plugins</button> <span id=\"hide-reload-plugins\" style=\"display: none;\"><button id=\"reload-plugins\">Reload plugins</button></span>\n<br/>\n\n<h2>Current staging area</h2>\n(Added but not committed changes.)\n<button id=\"commit\">Execute commit now</button>\n<button id=\"revert\">Execute revert</button>\n<div id=\"staging\"></div>\n\n<div style=\"display: none;\">\n-----------------------------------------------------------------------------------------------------------\nThese objects are not finished so they are hidden in browser\n\n\n<h2>Internet Aliases</h2>\n\n<button onclick=\"toggleVisible('help-internet-alias')\">toggle help</button><br/>\n<div id=\"help-internet-alias\" style=\"display: none;\">\n<p>Internet aliases can be used to transform a repo cache path to turn to a different real world URL. Two paths separated by a space is a valid alias entry.\nLines starting with # are comments.</p>\n\n<p>The path specified are paths within the repo cache. So it is possible to even change the protocol of the access.</p> \n\n<p>Using aliases it is possible to use a specific mirror of a project.</p>\n\nFormat example to connect a private mirror instead of the original:\n<pre>\n/proxy/https/eclipse.org/ /proxy/https/qgears.com/private-eclipse-mirror/\n</pre>\n</div>\n<textarea id=\"internet-alias\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit-internet-alias\">Submit target aliases</button>\n<br/>\n\n<h2>maven repos</h2>\n\n<p>Maven repos are configured by a name and a remote url. After configuration the remote url will be accessible through the /maven/{name}/ path in the repo cache.</p>\n\nFormat example:\n<pre>\nmavencentral https://repo.maven.apache.org/maven2/\n</pre>\n<textarea id=\"maven\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit-maven\">Submit maven repos</button>\n<br/>\n\n<h2>P2 repositories</h2>\n\nFormat:\n<pre>repoName file primaryHost selectedMirror repoMode repoDesc</pre>\n</div>\n");
	}


	@Override
	protected String getTitleFragment() {
		return getQuery().rc.getAccessRules().getName()+" Configuration";
	}

	public void handle() throws IOException {
		QueryResponse resp=generate();
		resp.contentType="text/html; utf-8";
		getQuery().reply(resp);
	}
	
}
