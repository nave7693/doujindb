package org.dyndns.doujindb.plug.event;

import org.dyndns.doujindb.plug.Plugin;

public class PluginAdapter implements PluginListener
{
	@Override
	public void pluginInstalled(Plugin plugin) { }

	@Override
	public void pluginUninstalled(Plugin plugin) { }

	@Override
	public void pluginStarted(Plugin plugin) { }

	@Override
	public void pluginStopped(Plugin plugin) { }

	@Override
	public void pluginUpdated(Plugin plugin) { }
}
