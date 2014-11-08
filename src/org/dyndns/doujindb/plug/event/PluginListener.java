package org.dyndns.doujindb.plug.event;

import org.dyndns.doujindb.plug.Plugin;

public interface PluginListener
{
	public void pluginInstalled(Plugin plugin);
	public void pluginUninstalled(Plugin plugin);
	public void pluginStarted(Plugin plugin);
	public void pluginStopped(Plugin plugin);
	public void pluginUpdated(Plugin plugin);
}
