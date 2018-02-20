package hu.qgears.repocache.log;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.Slot;
import hu.qgears.quickjs.qpage.QLabel;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.repocache.ClientQuery;

public class AccessLogPage extends hu.qgears.quickjs.utils.AbstractQPage
{
	ClientQuery query;
	public AccessLogPage(ClientQuery query) {
		super();
		this.query=query;
	}
	@Override
	protected void writeHeaders() {
		super.writeHeaders();
		write("<script language=\"javascript\" type=\"text/javascript\">\nfunction toggleVisible(id)\n{\n\tvar x = document.getElementById(id);\n    if (x.style.display === \"none\") {\n        x.style.display = \"block\";\n    } else {\n        x.style.display = \"none\";\n    }\n}\n</script>\n<title>");
		writeHtml(getTitleFragment());
		write("</title>\n");
	}
	protected String getTitleFragment() {
		return query.rc.getConfiguration().getName()+" Access Log";
	}
	@Override
	protected void writeBody() {
		write("<h1>");
		writeHtml(query.rc.getConfiguration().getName());
		write(" Access Logs</h1>\n<a href=\"../\">Repo cache root folder (../)</a><br/>\n\n");
		writeDiv("fromCache");
		writeDiv("missingCache");
		writeDiv("didNotChange");
		writeDiv("updated");
		writeDiv("errorDownload");
		writeDiv("localOnly");
	}
	private void writeDiv(String string) {
		write("<h2>");
		writeHtml(string);
		write(" <button onclick=\"toggleVisible('vis-");
		writeHtml(string);
		write("')\">toggle visible</button></h2>\n<div id=\"vis-");
		writeHtml(string);
		write("\">\nList:\n<div id=\"");
		writeHtml(string);
		write("\"></div>\n</div>\n\n");
	}
	@Override
	protected void initQPage(QPage page) {
		addPageElement("fromCache", query.rc.accessLog.fromCache);
		addPageElement("missingCache", query.rc.accessLog.missingCache);
		addPageElement("didNotChange", query.rc.accessLog.didNotChange);
		addPageElement("updated", query.rc.accessLog.updated);
		addPageElement("errorDownload", query.rc.accessLog.errorDownload);
		addPageElement("localOnly", query.rc.accessLog.localOnly);
	}
	private void addPageElement(String id, final LogEventList list) {
		final QLabel l=new QLabel(page, id);
		final UtilEventListener<LogEventList> listener=new UtilEventListener<LogEventList>() {
			@Override
			public void eventHappened(LogEventList msg) {
				page.submitToUI(new Runnable() {
					
					@Override
					public void run() {
						updateLabel(l, list);
					}
				});
			}
		};
		list.changed.addListener(listener);
		page.disposedEvent.addOnReadyHandler(new Slot<SignalFuture<QPage>>() {
			
			@Override
			public void signal(SignalFuture<QPage> value) {
				list.changed.removeListener(listener);
			}
		});
		updateLabel(l, list);
	}
	private void updateLabel(QLabel l, LogEventList list) {
		StringBuilder sb=new StringBuilder();
		sb.append("<pre>");
		list.dumpTo(sb);
		sb.append("</pre>");
		l.innerhtml.setPropertyFromServer(sb.toString());
	}
}
