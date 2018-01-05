package hu.qgears.repocache;

import hu.qgears.tools.Tools;

public class RepoCacheMain {
	public static void main(String[] args) {
		registerAll(Tools.getInstance());
		Tools.main(args);
	}
	public static void registerAll(Tools tools)
	{
		tools.register(new RepoCacheTool());
	}
}
