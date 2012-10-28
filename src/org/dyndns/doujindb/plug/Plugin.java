package org.dyndns.doujindb.plug;

import javax.swing.Icon;
import javax.swing.JComponent;

/**  
* Plugin.java - Every plugin must implement this - No Exceptions
* @author  nozomu
* @version 1.0
*/
public abstract class Plugin
{
	public abstract String getUUID();
	
	public abstract Icon getIcon();
	
	public abstract String getName();
	
	public abstract String getDescription();
	
	public abstract String getVersion();
	
	public abstract String getAuthor();
	
	public abstract String getWeblink();
	
	public abstract JComponent getUI();
	
	protected abstract void install() throws PluginException;
	
	protected abstract void update() throws PluginException;
	
	protected abstract void uninstall() throws PluginException;

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Plugin))
			return false;
		else
			return ((Plugin)obj).getUUID().equals(getUUID());
	}
}
