package org.dyndns.doujindb.plug;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JComponent;

/**  
* Plugin.java - Every plugin must implement this - No Exceptions
* @author  nozomu
* @version 1.0
*/
public abstract class Plugin
{
	public enum State
	{
		RUNNING,
		STOPPED,
		LOADING,
		UPDATING
	}
	
	protected final File PLUGIN_HOME;
	protected final File CONFIG_FILE;
	
	{
		PLUGIN_HOME = new File(PluginManager.PLUGIN_HOME, getNamespace());
		CONFIG_FILE = new File(PLUGIN_HOME, "config.xml");
	}

	public final String getNamespace() {
		return getClass().getCanonicalName();
	}

	public abstract Icon getIcon();
	
	public abstract String getName();
	
	public abstract String getDescription();
	
	public abstract String getVersion();
	
	public abstract String getAuthor();
	
	public abstract String getWeblink();
	
	public abstract JComponent getUI();

	public abstract State getState();

	protected abstract void doInstall() throws PluginException;
	
	protected abstract void doUpdate() throws PluginException;
	
	protected abstract void doUninstall() throws PluginException;

	protected void doStartup() throws PluginException { }
	
	protected void doShutdown() throws PluginException { }

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Plugin))
			return false;
		else
			return ((Plugin)obj).getNamespace().equals(getNamespace());
	}
	
	@Override
	public int hashCode() {
		return getNamespace().hashCode();
	}
}
