package hu.qgears.repocache.qpage;

import org.apache.commons.lang.StringEscapeUtils;

import hu.qgears.rtemplate.runtime.ICodeGeneratorContext;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;

public class HtmlTemplate extends RAbstractTemplatePart{

	public HtmlTemplate(RAbstractTemplatePart parent) {
		super(parent);
	}
	
	public HtmlTemplate(ICodeGeneratorContext codeGeneratorContext) {
		super(codeGeneratorContext);
	}

	protected void writeJSValue(String text) {
		writeObject(StringEscapeUtils.escapeJavaScript(text));
	}

}
