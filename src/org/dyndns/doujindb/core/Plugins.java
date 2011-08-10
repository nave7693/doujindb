package org.dyndns.doujindb.core;

import java.util.Vector;

import org.dyndns.doujindb.plug.Plugin;
import org.dyndns.doujindb.plug.impl.mugimugi.DoujinshiDBScanner;


/**  
* Plugins.java - Handle every plugin
* @author  nozomu
* @version 1.0
*/
public final class Plugins
{
	private static Vector<Plugin> plugins;
	
	public static void init()
	{
		plugins = new Vector<Plugin>();
		plugins.add(new DoujinshiDBScanner());
	}
	
	public static Iterable<Plugin> getPlugins()
	{
		return plugins;
	}
}
