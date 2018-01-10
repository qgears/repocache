package hu.qgears.repocache.qpage.example;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.repocache.qpage.QLabel;
import hu.qgears.repocache.qpage.QPage;
import hu.qgears.repocache.qpage.QSelect;
import hu.qgears.repocache.qpage.QSelectCombo;
import hu.qgears.repocache.qpage.QTextEditor;

/**
 * A simple example of a QPage based web application. 
 */
public class QExample02 extends AbstractQPage
{
	private int n=1;
	QSelect[] selarr=new QSelect[5];
	@Override
	protected void initQPage(final QPage page) {
		final QTextEditor number=new QTextEditor(page, "number");
		number.text.setPropertyFromServer("1");
		final QLabel result=new QLabel(page, "result");
		result.innerhtml.setPropertyFromServer("");
		number.text.clientChangedEvent.addListener(new UtilEventListener<String>() {
			@Override
			public void eventHappened(String msg) {
				try {
					n=Integer.parseInt(number.text.getProperty());
					if(n<1)
					{
						throw new NumberFormatException();
					}
					for(QSelect sel: selarr)
					{
						List<String> empty=new ArrayList<>();
						sel.options.setPropertyFromServer(empty);
					}
					setOptions(0, selarr[0], "");
					result.innerhtml.setPropertyFromServer("");
				} catch (Exception e) {
					result.innerhtml.setPropertyFromServer(StringEscapeUtils.escapeHtml("Value must be a positive number!"));
				}	
			}
		});
		QSelect prev=null;
		for(int i=0;i<5;++i)
		{
			final int index=i;
			final QSelect select=createQSelect(page, "select-"+i);
			selarr[i]=select;
			if(prev==null)
			{
				setOptions(index, select, "");
			}else
			{
				List<String> options=new ArrayList<>();
				options.add("Invalid");
				select.options.setPropertyFromServer(options);
				final QSelect prev1=prev;
				prev.selected.clientChangedEvent.addListener(new UtilEventListener<Integer>() {
					
					@Override
					public void eventHappened(Integer msg) {
						setOptions(index, select, prev1.options.getProperty().get(msg));
					}
				});
			}
			prev=select;
		}
		selarr[selarr.length-1].selected.clientChangedEvent.addListener(new UtilEventListener<Integer>() {
			public void eventHappened(Integer msg) {
				result.innerhtml.setPropertyFromServer(selarr[selarr.length-1].options.getProperty().get(msg));
			};
		});
	}
	protected QSelect createQSelect(QPage page, String string) {
		return new QSelectCombo(page, string);
	}
	private void setOptions(int i, QSelect select, String prefix) {
		List<String> options=new ArrayList<>();
		for(int j=0;j<n;++j)
		{
			options.add(prefix+"option-"+j);
		}
		select.options.setPropertyFromServer(options);
		select.selected.setPropertyFromServer(n/2);
		for(int j=i+1;j<selarr.length;++j)
		{
			if(selarr[j]!=null)
			{
				selarr[j].options.setPropertyFromServer((List<String>)new ArrayList<String>());
			}
		}
	}
	@Override
	protected void writeBody() {
		write("<h1>QPage example with ");
		writeObject(getClass().getSimpleName());
		write("</h1>\nNumber of entries within the selectors:\n<input id=\"number\" size=\"100\"></input>\n<div id=\"result\"></div>\n");
		for(int i=0; i<selarr.length; ++i)
		{
			selarr[i].generateExampleHtmlObject(this);
		}
	}
}
