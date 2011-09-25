package org.dyndns.doujindb.plug;

import java.awt.Component;
import javax.swing.Icon;

/**  
* Plugin.java - Every plugin must implement this - No Exceptions
* @author  nozomu
* @version 1.0
*/
public interface Plugin
{
	public Icon getIcon();
	
	public String getName();
	
	public String getDescription();
	
	public String getVersion();
	
	public String getAuthor();
	
	public String getWeblink();
	
	public Component getUI();
}
