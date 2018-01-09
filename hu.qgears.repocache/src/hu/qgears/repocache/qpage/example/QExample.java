package hu.qgears.repocache.qpage.example;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.repocache.qpage.HtmlTemplate;
import hu.qgears.repocache.qpage.QButton;
import hu.qgears.repocache.qpage.QLabel;
import hu.qgears.repocache.qpage.QPage;
import hu.qgears.repocache.qpage.QPageManager;
import hu.qgears.repocache.qpage.QTextEditor;
import hu.qgears.repocache.utils.InMemoryPost;

public class QExample extends AbstractHandler
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
			baseRequest.setHandled(true);
			response.setContentType("text/html; charset=utf-8");
			Writer wr=new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
			new HtmlTemplate(wr){
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
						final QTextEditor accessRules=new QTextEditor(page, "texted");
						accessRules.text.setPropertyFromServer("Example text to edit");
						QButton buttonAccess=new QButton(page, "submit");
						buttonAccess.clicked.addListener(new UtilEventListener<QButton>() {
							@Override
							public void eventHappened(QButton msg) {
								// getHttpQuery().rc.getAccessRules().setConfig(accessRules.text.getProperty());
							}
						});
						final QLabel l=new QLabel(page, "mylabel");
						accessRules.text.clientChangedEvent.addListener(new UtilEventListener<String>() {
							public void eventHappened(String msg) {
								l.innerhtml.setPropertyFromServer(StringEscapeUtils.escapeHtml(msg));
							};
						});
						
						final QLabel counter=new QLabel(page, "counter");
						new Thread("QExample Counter")
						{
							public void run() {
								while(!page.disposedEvent.isDone())
								{
									counter.getPage().submitToUI(new Runnable() {
										
										@Override
										public void run() {
											counter.innerhtml.setPropertyFromServer(""+System.currentTimeMillis());
											// TODO Auto-generated method stub
											
										}
									});
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							};
						}
						.start();
						l.innerhtml.setPropertyFromServer("initial value");;
						generateHtmlContent();
					}
				}

				public void generate() throws Exception {
					dogenerate();
				}

				private void generateHtmlContent() {
					write("<html>\n<head>\n");
					page.writeHeaders(this);
					write("</head>\n<body>\n<h1>QPage example page</h1>\n\n<h2>Text editor with feedback</h2>\n\n<textarea id=\"texted\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit\">Submit button</button>\n<br/>\n<div id=\"mylabel\">static content</div>\n<div id=\"counter\">static content</div>\n</body>\n</html>\n");
				}
				
			}.generate();
			wr.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
