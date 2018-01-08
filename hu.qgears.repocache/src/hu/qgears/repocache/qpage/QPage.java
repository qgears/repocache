package hu.qgears.repocache.qpage;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import hu.qgears.repocache.utils.InMemoryPost;

/**
 * @author rizsi
 *
 */
public class QPage implements Closeable {
	public static String idAttribute = QPage.class.getSimpleName();
	private String identifier = "id";
	private volatile boolean active = true;
	private Map<String, QComponent> components = new HashMap<>();
	private Object syncObject = new Object();
	private int currentMessageIndex = 0;
	private int serverstateindex = 0;
	private HtmlTemplate currentTemplate;
	public boolean inited;
	private static long TIMEOUT_DISPOSE=30000;
	
	class MessageFramingTemplate extends HtmlTemplate {

		public MessageFramingTemplate(HtmlTemplate parent) {
			super(parent);
		}

		public void openMessage() {
			write("page.processServerMessage(");
			writeObject(serverstateindex);
			write(",function(page)\n{\n");
			serverstateindex++;
		}

		public void closeMessage() {
			write("});\n");
		}

	}

	class Message {
		HtmlTemplate parent;
		InMemoryPost post;
		int index;

		public Message(HtmlTemplate parent, InMemoryPost post) throws NumberFormatException, IOException {
			super();
			this.parent = parent;
			this.post = post;
			index = Integer.parseInt(post.getParameter("messageindex"));
		}

		public void executeOnThread() throws Exception {
			long t = System.currentTimeMillis();
			synchronized (syncObject) {
				// Proper ordering of messages!
				while (index != currentMessageIndex) {
					syncObject.wait(10000);
					if (System.currentTimeMillis() > t + 10000) {
						// TODO crash the client! User feedback of internal
						// error!
						throw new TimeoutException();
					}
				}
				MessageFramingTemplate msft = new MessageFramingTemplate(parent);
				currentTemplate=parent;
				msft.openMessage();
				String cid = post.getParameter("component");
				QComponent ed = components.get(cid);
				ed.handle(parent, post);
				currentTemplate=null;
				msft.closeMessage();
				currentMessageIndex++;
				syncObject.notifyAll();
			}
		}
	}
	private TimerTask disposeTimer;
	public QPage(QPageManager qpm) {
		identifier = qpm.createId();
		qpm.register(identifier, this);
		reinitDisposeTimer();
	}

	private void reinitDisposeTimer() {
		if(disposeTimer!=null)
		{
			disposeTimer.cancel();
			disposeTimer=null;
		}
		disposeTimer=new TimerTask() {
			
			@Override
			public void run() {
				dispose();
			}
		};
		QPageManager.disposeTimer.schedule(disposeTimer, TIMEOUT_DISPOSE);
	}

	public void writeHeaders(final HtmlTemplate parent) {
		new HtmlTemplate(parent) {
			public void generate() {
				write("<script language=\"javascript\" type=\"text/javascript\">\n\nclass QPage\n{\n\tconstructor()\n\t{\n\t\tthis.messageindex=0;\n\t\tthis.serverstateindex=0;\n\t\tthis.waitingMessages={};\n\t\tthis.components={};\n\t}\n\tprocessServerMessage(serverstate, message)\n\t{\n\t\tif(serverstate==this.serverstateindex)\n\t\t{\n\t\t\tmessage(this);\n\t\t\tthis.serverstateindex++;\n\t\t\twhile(this.waitingMessages[this.serverstateindex])\n\t\t\t{\n\t\t\t\tthis.waitingMessages[this.serverstateindex](this);\n\t\t\t\tdelete this.waitingMessages[this.serverstateindex];\n\t\t\t\tthis.serverstateindex++;\n\t\t\t}\n\t\t}else\n\t\t{\n\t\t\tthis.waitingMessages[serverstate, message];\n\t\t}\n\t}\n\tstart()\n\t{\n\t\tthis.query();\n\t}\n\tquery()\n\t{\n\t\tvar xhr = new XMLHttpRequest();\n\t\txhr.qpage=this;\n\t\txhr.responseType = \"text\";\n\t\txhr.onreadystatechange = function() {\n\t\t\tif (this.readyState == 4 && this.status == 200) {\n\t\t\t\tvar page=this.qpage;\n\t\t\t\teval(this.responseText);\n\t\t\t}\n\t\t}.bind(xhr);\n\t\tvar FD = new FormData();\n\t\tFD.append(\"QPage\", \"");
				writeObject(identifier);
				write("\");\n\t\tFD.append(\"periodic\", \"true\");\n\t\txhr.open(\"POST\",'?QPage=");
				writeObject(identifier);
				write("');\n\t\txhr.send(FD);\n\t}\n\tcreateFormData(component)\n\t{\n\t\tvar FD = new FormData();\n\t\tFD.append(\"QPage\", \"");
				writeObject(identifier);
				write("\");\n\t\tFD.append(\"component\", component.identifier);\n\t\treturn FD;\n\t}\n\tsend(FD)\n\t{\n\t\tFD.append(\"messageindex\", this.messageindex);\n\t\tthis.messageindex++;\n\t\tvar xhr = new XMLHttpRequest();\n\t\txhr.qpage=this;\n\t\txhr.responseType = \"text\";\n\t\txhr.onreadystatechange = function() {\n\t\t\tif (this.readyState == 4 && this.status == 200) {\n\t\t\t\tvar page=this.qpage;\n\t\t\t\teval(this.responseText);\n\t\t\t}\n\t\t}.bind(xhr);\n\t\txhr.open(\"POST\",'?QPage=");
				writeObject(identifier);
				write("');\n\t\txhr.send(FD);\n\t}\n}\nclass QComponent\n{\n\tconstructor(page, identifier)\n\t{\n\t\tthis.page=page;\n\t\tpage.components[identifier]=this;\n\t\tthis.identifier=identifier;\n\t\tthis.dom=document.getElementById(identifier);\n\t\tif(!this.dom)\n\t\t{\n\t\t\tconsole.error(\"Dom object missing: '\"+identifier+\"'\");\n\t\t}\n\t\tthis.addDomListeners();\n\t}\n}\nwindow.addEventListener(\"load\", function(){\n\tvar page=new QPage();\n");
				for (QComponent c : components.values()) {
					c.init(parent);
				}
				// TODO periodic refresh is not started yet! page.start();
				write("}, false);\n</script>\n");
				inited=true;
			}
		}.generate();
		QButton.generateHeader(parent);
		QTextEditor.generateHeader(parent);
		QLabel.generateHeader(parent);
	}

	public boolean handle(HtmlTemplate parent, InMemoryPost post) throws IOException {
		if (identifier.equals(post.getParameter("QPage"))) {
			if ("true".equals(post.getParameter("periodic"))) {
				handlePeriodicQuery(parent);
				return true;
			}
			Message m = new Message(parent, post);
			try {
				m.executeOnThread();
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		return true;
	}

	public void executeSynchronized(Runnable r) {
		synchronized (syncObject) {
			r.run();
		}
	}

	private void handlePeriodicQuery(HtmlTemplate parent) {
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new HtmlTemplate(parent) {
			public void generate() {
				write("console.info(\"Hello QPage reply! \"+page);\n");
				if (active) {
					write("page.query();\n");
				}
			}
		}.generate();
	}

	@Override
	public void close() {
		active = false;
	}

	public void add(QComponent qTextEditor) {
		components.put(qTextEditor.getId(), qTextEditor);
	}

	public HtmlTemplate getCurrentTemplate() {
		return currentTemplate;
	}

	/**
	 * Dispose is called on the session dispose event from the Web Servers thread.
	 */
	public void dispose() {
	}

}
