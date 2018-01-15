package hu.qgears.repocache;

import hu.qgears.repocache.transparentproxy.TransparentProxyTool;
import joptsimple.tool.AbstractTools;

public class RepoCacheMain extends AbstractTools {

	
	public static void main(String[] args) {
		new RepoCacheMain().mainEntryPoint(args);
	}
	
	@Override
	protected void registerTools() {
		register(new RepoCacheTool());
		register(new TransparentProxyTool());
	}
}
