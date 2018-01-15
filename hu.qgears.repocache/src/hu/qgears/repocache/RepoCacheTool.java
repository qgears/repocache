package hu.qgears.repocache;

import joptsimple.tool.AbstractTool;

public class RepoCacheTool extends AbstractTool {

	@Override
	public String getId() {
		return "repocache";
	}

	@Override
	public String getDescription() {
		return "Repo cache Version controlled dependency repository cache https://github.com/qgears/repocache";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		return RepoCache.exec((CommandLineArgs) a);
	}

	@Override
	protected IArgs createArgsObject() {
		return new CommandLineArgs();
	}

}
