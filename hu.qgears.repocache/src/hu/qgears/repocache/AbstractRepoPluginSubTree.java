package hu.qgears.repocache;

import hu.qgears.repocache.config.RepoConfiguration.PluginDef;

/**
 * A plugin that can be applied on a subtree.
 * @author rizsi
 *
 */
abstract public class AbstractRepoPluginSubTree extends AbstractRepoPlugin {

	/**
	 * Initialize plugin with the configuration parameters.
	 * @param pd
	 */
	abstract public void init(PluginDef pd);

}
