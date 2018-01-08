package hu.qgears.repocache.log;

import java.util.LinkedList;

import hu.qgears.commons.UtilEvent;

public class LogEventList {
	private LinkedList<String> events=new LinkedList<>();
	public final UtilEvent<LogEventList> changed=new UtilEvent<>();
	public void add(String msg) {
		synchronized (events)
		{
			events.add(msg);
			while(events.size()>100)
			{
				events.removeFirst();
			}
		}
		changed.eventHappened(this);
	}
	public void dumpTo(StringBuilder sb) {
		synchronized (events)
		{
			for(String s: events)
			{
				sb.append(s);
				sb.append("\n");
			}
		}
	}

}
