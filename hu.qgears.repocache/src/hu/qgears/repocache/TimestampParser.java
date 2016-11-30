package hu.qgears.repocache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TimestampParser extends DefaultHandler
{
	private static Log log=LogFactory.getLog(TimestampParser.class);
	private long timestamp=System.currentTimeMillis();
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		try {
			if(qName.equals("property"))
			{
				if("p2.timestamp".equals(attributes.getValue("name")))
				{
					timestamp=Long.parseLong(attributes.getValue("value"));
				}
			}
		} catch (NumberFormatException e) {
			log.error("Timestamp must be long, value: " + attributes.getValue("value"), e);
		}
		super.startElement(uri, localName, qName, attributes);
	}
	public long getTimestamp() {
		return timestamp;
	}
}
