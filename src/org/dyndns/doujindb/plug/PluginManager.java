package org.dyndns.doujindb.plug;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.log.*;

/**  
* PluginManager.java - DoujinDB Plugin Manager.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("unchecked")
public final class PluginManager
{
	private static Set<Plugin> plugins = new HashSet<Plugin>();
	
	private static int PLUGIN_TIMEOUT = 10;
	private static final File PLUGIN_INDEX = new File(Core.DOUJINDB_HOME, "plugins.xml");

	private static final String TAG = "PluginManager : ";

	private PluginManager() { }
	
	static
	{
		/*
		 * Register a shutdown hook to handle the shutdown of this JVM for every Plugin
		 * If the Plugin doesn't shutdown after PLUGIN_TIMEOUT (seconds), skip it
		 */
		Logger.logInfo(TAG + "registering shutdown hook for this JVM ...");
		Runtime.getRuntime().addShutdownHook(new Thread("pluginmanager-shutdownhook")
		{
			@Override
			public void run()
			{
				shutdown();
			}
		});
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
	
	public static void discovery()
	{
		Logger.logInfo(TAG + "discovering plugins ...");
		// TODO : Dynamic discovery
		for(String plugin : new String[]{
				"org.dyndns.doujindb.plug.impl.mugimugi.DoujinshiDBScanner"
			})
			try {
				Logger.logInfo(TAG + "found '" + plugin + "'.");
				Plugin plug = (Plugin) Class.forName(plugin).newInstance();
				plugins.add(plug);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public static void load()
	{
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PLUGIN_INDEX));
			Set<String> plugins_names = (Set<String>) ois.readObject();
			
			for(String plugin : plugins_names)
			try {
				plugins.add((Plugin) Class.forName(plugin).newInstance());
			} catch (RuntimeException re) {
				Logger.logError("Failed to load plugin '" + plugin + "' : " + re.getMessage(), re);
			} catch (InstantiationException ie) {
				Logger.logError("Failed to load plugin '" + plugin + "' : " + ie.getMessage(), ie);
			} catch (IllegalAccessException iae) {
				Logger.logError("Failed to load plugin '" + plugin + "' : " + iae.getMessage(), iae);
			}
			
			ois.close();
		} catch (FileNotFoundException fnfe) {
			Logger.logError("Failed to load plugins : " + fnfe.getMessage(), fnfe);
		} catch (IOException ioe) {
			Logger.logError("Failed to load plugins : " + ioe.getMessage(), ioe);
		} catch (ClassNotFoundException cnfe) {
			Logger.logError("Failed to load plugins : " + cnfe.getMessage(), cnfe);
		}
	}
	
	public static void save()
	{
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PLUGIN_INDEX));
			
			Set<String> plugins_names = new HashSet<String>();
			for(Plugin plugin : plugins)
				plugins_names.add(plugin.getClass().getCanonicalName());
			
			oos.writeObject(plugins_names);
			oos.close();
		} catch (FileNotFoundException fnfe) {
			try { PLUGIN_INDEX.createNewFile(); } catch (IOException ioe) { }
			Logger.logWarning("Failed to save plugins : " + fnfe.getMessage(), fnfe);
		} catch (IOException ioe) {
			Logger.logError("Failed to save plugins : " + ioe.getMessage(), ioe);
		}
	}
	
	public static void startup()
	{
		ExecutorService executor = Executors.newCachedThreadPool();
		for(final Plugin plugin : plugins)
		{
			Callable<Void> task = new Callable<Void>()
			{
				public Void call()
				{
					try {
						plugin.startup();
						Logger.logInfo("Plugin '" + plugin.getName() + "' started");
						return null;
					} catch (PluginException pe) {
						return null;
					}
				}
			};
			Future<Void> future = executor.submit(task);
			try
			{
				future.get(PLUGIN_TIMEOUT, TimeUnit.SECONDS);
			} catch (TimeoutException te) {
				Logger.logWarning("TimeoutException : Cannot startup plugin '" + plugin.getName() + "'", te);
				te.printStackTrace();
			} catch (InterruptedException ie) {
				Logger.logWarning("InterruptedException : Cannot startup plugin '" + plugin.getName() + "'", ie);
				ie.printStackTrace();
			} catch (ExecutionException ee) {
				Logger.logWarning("ExecutionException : Cannot startup plugin '" + plugin.getName() + "'", ee);
				ee.printStackTrace();
			} finally {
			   future.cancel(true);
			}
		}
	}
	
	private static void shutdown()
	{
		ExecutorService executor = Executors.newCachedThreadPool();
		for(final Plugin plugin : plugins)
		{
			Callable<Void> task = new Callable<Void>()
			{
				public Void call()
				{
					try {
						plugin.shutdown();
						Logger.logInfo("Plugin '" + plugin.getName() + "' stopped");
						return null;
					} catch (PluginException pe) {
						return null;
					}
				}
			};
			Future<Void> future = executor.submit(task);
			try
			{
				future.get(PLUGIN_TIMEOUT, TimeUnit.SECONDS);
			} catch (TimeoutException te) {
				Logger.logWarning("TimeoutException : Cannot shutdown plugin '" + plugin.getName() + "'", te);
				te.printStackTrace();
			} catch (InterruptedException ie) {
				Logger.logWarning("InterruptedException : Cannot shutdown plugin '" + plugin.getName() + "'", ie);
				ie.printStackTrace();
			} catch (ExecutionException ee) {
				Logger.logWarning("ExecutionException : Cannot shutdown plugin '" + plugin.getName() + "'", ee);
				ee.printStackTrace();
			} finally {
			   future.cancel(true);
			}
		}
	}
}
