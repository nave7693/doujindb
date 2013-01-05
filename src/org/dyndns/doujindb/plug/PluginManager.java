package org.dyndns.doujindb.plug;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.log.Level;

/**  
* PluginManager.java - DoujinDB Plugin Manager.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("unchecked")
public final class PluginManager
{
	private static Set<Plugin> plugins;
	
	static
	{
		plugins = new HashSet<Plugin>();
		
		for(String plugin : new String[]{
			"org.dyndns.doujindb.plug.impl.mugimugi.DoujinshiDBScanner",
			"org.dyndns.doujindb.plug.impl.imagescanner.ImageScanner"
		})
		try {
			plugins.add((Plugin) Class.forName(plugin).newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
		File file = new File(System.getProperty("doujindb.home"),"doujindb.plugins");
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			Set<String> plugins_names = (Set<String>) ois.readObject();
			
			for(String plugin : plugins_names)
			try {
				plugins.add((Plugin) Class.forName(plugin).newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			ois.close();
		} catch (FileNotFoundException fnfe) {
			Core.Logger.log("Failed to load plugins : " + fnfe.getMessage() + ".", Level.WARNING);
		} catch (IOException ioe) {
			Core.Logger.log("Failed to load plugins : " + ioe.getMessage() + ".", Level.ERROR);
		} catch (ClassNotFoundException cnfe) {
			Core.Logger.log("Failed to load plugins : " + cnfe.getMessage() + ".", Level.ERROR);
		}
	}
	
	public static void install(Plugin plugin) throws PluginException
	{
		if(plugins.contains(plugin))
			throw new PluginException("Plugin '" + plugin.getName() + "' is already installed.");
		plugin.install();
		plugins.add(plugin);
		save();
	}
	
	public static void update(Plugin plugin) throws PluginException
	{
		if(!plugins.contains(plugin))
			throw new PluginException("Plugin '" + plugin.getName() + "' is not installed.");
		plugin.update();
	}
	
	public static void uninstall(Plugin plugin) throws PluginException
	{
		if(!plugins.contains(plugin))
			throw new PluginException("Plugin '" + plugin.getName() + "' is not installed.");
		plugin.uninstall(); //TODO force removal even if PluginException is thrown?
		plugins.remove(plugin);
		save();
	}
	
	public static Iterable<Plugin> plugins()
	{
		return plugins;
	}
	
	private static void save()
	{
		File file = new File(System.getProperty("doujindb.home"),"doujindb.plugins");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(plugins);
		} catch (FileNotFoundException fnfe) {
			try { file.createNewFile(); } catch (IOException ioe) { }
			Core.Logger.log("Failed to save plugins : " + fnfe.getMessage() + ".", Level.WARNING);
		} catch (IOException ioe) {
			Core.Logger.log("Failed to save plugins : " + ioe.getMessage() + ".", Level.ERROR);
		}
	}
}