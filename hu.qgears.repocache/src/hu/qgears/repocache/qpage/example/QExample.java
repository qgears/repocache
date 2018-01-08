package hu.qgears.repocache.qpage.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.repocache.ClientQueryHttp;
import hu.qgears.repocache.qpage.HtmlTemplate;
import hu.qgears.repocache.qpage.QButton;
import hu.qgears.repocache.qpage.QLabel;
import hu.qgears.repocache.qpage.QPage;
import hu.qgears.repocache.qpage.QPageManager;
import hu.qgears.repocache.qpage.QTextEditor;
import hu.qgears.repocache.utils.InMemoryPost;
import hu.qgears.rtemplate.runtime.ICodeGeneratorContext;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;
import hu.qgears.rtemplate.runtime.TemplateTracker;

public class QExample extends AbstractHandler implements ICodeGeneratorContext
{
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		
        // Specify the Session ID Manager
        HashSessionIdManager idmanager = new HashSessionIdManager();
        server.setSessionIdManager(idmanager);

        // Sessions are bound to a context.
        ContextHandler context = new ContextHandler("/");
        server.setHandler(context);

        // Create the SessionHandler (wrapper) to handle the sessions
        HashSessionManager manager = new HashSessionManager();
        SessionHandler sessions = new SessionHandler(manager);
        sessions.addEventListener(QPageManager.createSessionListener());
        context.setHandler(sessions);
        sessions.setHandler(new QExample());
        server.start();
        server.join();
	}

	@Override
	public void handle(String target, final Request baseRequest, HttpServletRequest request, 
			final HttpServletResponse response)
			throws IOException, ServletException {
 		HttpSession sess=baseRequest.getSession();
		final QPageManager qpm=QPageManager.getManager(sess);
		final QPage page0=qpm.getPage(baseRequest);
		try {
			new HtmlTemplate(this){
				QPage page;
				public void dogenerate() throws Exception {
					if(page0!=null)
					{
						InMemoryPost post=new InMemoryPost(baseRequest);
						page0.handle(this, post);
						return;
					}else
					{
						page=new QPage(qpm);
						final QTextEditor accessRules=new QTextEditor(page, "access");
						accessRules.text.setProperty("access rules current value");
						QButton buttonAccess=new QButton(page, "submit-access");
						buttonAccess.clicked.addListener(new UtilEventListener<QButton>() {
							@Override
							public void eventHappened(QButton msg) {
								// getHttpQuery().rc.getAccessRules().setConfig(accessRules.text.getProperty());
							}
						});
						QButton buttonAccessReload=new QButton(page, "reload-access");
						buttonAccessReload.clicked.addListener(new UtilEventListener<QButton>() {
							@Override
							public void eventHappened(QButton msg) {
								 // accessRules.text.setProperty(getHttpQuery().rc.getAccessRules().getConfig());
							}
						});
						final QLabel l=new QLabel(page, "mylabel");
						accessRules.text.getPropertyChangedEvent().addListener(new UtilEventListener<String>() {
							public void eventHappened(String msg) {
								l.text.setProperty(msg);
							};
						});
						l.text.setProperty("initial value");;
						generateHtmlContent();
					}
				}

				public void generate() throws Exception {
					dogenerate();
					getTemplateState().flush();
					baseRequest.setHandled(true);
					response.setContentType("text/html; charset=utf-8"); 
					response.getOutputStream().write(getTemplateState().getOut().toString().getBytes(StandardCharsets.UTF_8));
				}

				private void generateHtmlContent() {
					write("<html>\n<head>\n");
					page.writeHeaders(this);
					write("</head>\n<body>\n<h1>Repo Cache configuration2</h1>\n<a href=\"../\">Repo cache root folder (../)</a><br/>\n\n\n\n\n<h2>Access rules</h2>\n\n<textarea id=\"access\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit-access\">Submit access rules</button> <button id=\"reload-access\">Reload access rules</button>\n<br/>\n<div id=\"mylabel\">static content</div>\n</body>\n</html>\n");
				}
				
			}.generate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean needReport() {
		return false;
	}

	@Override
	public void createFile(String path, String o) {
	}

	@Override
	public void createReport(String path, String o, TemplateTracker tt) {
	}
}
