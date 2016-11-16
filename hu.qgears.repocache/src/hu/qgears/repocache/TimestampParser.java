package hu.qgears.repocache;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TimestampParser extends DefaultHandler
{
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.startElement(uri, localName, qName, attributes);
	}
	public long getTimestamp() {
		return timestamp;
	}
}
