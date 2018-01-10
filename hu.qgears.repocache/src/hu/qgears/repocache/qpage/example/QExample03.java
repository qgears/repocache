package hu.qgears.repocache.qpage.example;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.repocache.qpage.QLabel;
import hu.qgears.repocache.qpage.QPage;
import hu.qgears.repocache.qpage.QSelect;
import hu.qgears.repocache.qpage.QSelectCombo;
import hu.qgears.repocache.qpage.QSelectFastScroll;
import hu.qgears.repocache.qpage.QTextEditor;

/**
 * A simple example of a QPage based web application. 
 */
public class QExample03 extends QExample02
{

	@Override
	protected QSelect createQSelect(QPage page, String string) {
		return new QSelectFastScroll(page, string);
	}
}
