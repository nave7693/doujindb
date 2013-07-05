package org.dyndns.doujindb.plug;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.log.Level;

/**  
* PluginManager.java - DoujinDB Plugin Manager.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("unchecked")
public final class PluginManager
{
	private static Set<Plugin> plugins = new HashSet<Plugin>();
	
	private static int SHUTDOWN_TIMEOUT = 10;
	private static String PLUGIN_FILE = "plugins.bin";
	
	private PluginManager() { }
	
	static
	{
		/*
		 * Register a shutdown hook to handle the shutdown of this JVM for every Plugin
		 * If the Plugin doesn't shutdown after a TIMEOUT, skip it
		 */
		Runtime.getRuntime().addShutdownHook(new Thread(PluginManager.class.getName()+"$ShutdownHook")
		{
			@Override
			public void run()
			{
				shutdown();
			}
		});
		
		Core.Database.addDataBaseListener(new Listener());
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
		// TODO : Dynamic discovery
		// "org.dyndns.doujindb.plug.impl.imagescanner.ImageScanner"
		for(String plugin : new String[]{
				"org.dyndns.doujindb.plug.impl.mugimugi.DoujinshiDBScanner"
			})
			try {
				Plugin plug = (Plugin) Class.forName(plugin).newInstance();
				plugins.add(plug);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public static void load()
	{
		File file = new File(System.getProperty("doujindb.home"), PLUGIN_FILE);
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			Set<String> plugins_names = (Set<String>) ois.readObject();
			
			for(String plugin : plugins_names)
			try {
				plugins.add((Plugin) Class.forName(plugin).newInstance());
			} catch (RuntimeException re) {
				Core.Logger.log("Failed to load plugin [" + plugin + "] : " + re.getMessage() + ".", Level.ERROR);
			} catch (InstantiationException ie) {
				Core.Logger.log("Failed to load plugin [" + plugin + "] : " + ie.getMessage() + ".", Level.ERROR);
			} catch (IllegalAccessException iae) {
				Core.Logger.log("Failed to load plugin [" + plugin + "] : " + iae.getMessage() + ".", Level.ERROR);
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
	
	public static void save()
	{
		File file = new File(System.getProperty("doujindb.home"), PLUGIN_FILE);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			
			Set<String> plugins_names = new HashSet<String>();
			for(Plugin plugin : plugins)
				plugins_names.add(plugin.getClass().getCanonicalName());
			
			oos.writeObject(plugins_names);
			oos.close();
		} catch (FileNotFoundException fnfe) {
			try { file.createNewFile(); } catch (IOException ioe) { }
			Core.Logger.log("Failed to save plugins : " + fnfe.getMessage() + ".", Level.WARNING);
		} catch (IOException ioe) {
			Core.Logger.log("Failed to save plugins : " + ioe.getMessage() + ".", Level.ERROR);
		}
	}
	
	private static void startup()
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
						Core.Logger.log("[Plugin:'" + plugin.getName() + "'] started.", Level.DEBUG);
						return null;
					} catch (PluginException pe) {
						return null;
					}
				}
			};
			Future<Void> future = executor.submit(task);
			try
			{
				future.get(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
			} catch (TimeoutException te) {
				Core.Logger.log("TimeoutException : Cannot startup [Plugin:'" + plugin.getName() + "']", Level.WARNING);
				te.printStackTrace();
			} catch (InterruptedException ie) {
				Core.Logger.log("InterruptedException : Cannot startup [Plugin:'" + plugin.getName() + "']", Level.WARNING);
				ie.printStackTrace();
			} catch (ExecutionException ee) {
				Core.Logger.log("ExecutionException : Cannot startup [Plugin:'" + plugin.getName() + "']", Level.WARNING);
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
						Core.Logger.log("[Plugin:'" + plugin.getName() + "'] stopped.", Level.DEBUG);
						return null;
					} catch (PluginException pe) {
						return null;
					}
				}
			};
			Future<Void> future = executor.submit(task);
			try
			{
				future.get(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
			} catch (TimeoutException te) {
				Core.Logger.log("TimeoutException : Cannot shutdown [Plugin:'" + plugin.getName() + "']", Level.WARNING);
				te.printStackTrace();
			} catch (InterruptedException ie) {
				Core.Logger.log("InterruptedException : Cannot shutdown [Plugin:'" + plugin.getName() + "']", Level.WARNING);
				ie.printStackTrace();
			} catch (ExecutionException ee) {
				Core.Logger.log("ExecutionException : Cannot shutdown [Plugin:'" + plugin.getName() + "']", Level.WARNING);
				ee.printStackTrace();
			} finally {
			   future.cancel(true);
			}
		}
	}
	
	private static final class Listener implements DataBaseListener
	{
		@Override
		public void recordAdded(Record rcd) { }

		@Override
		public void recordDeleted(Record rcd) { }

		@Override
		public void recordUpdated(Record rcd, UpdateData data) { }

		@Override
		public void recordRecycled(Record rcd) { }

		@Override
		public void recordRestored(Record rcd) { }

		@Override
		public void databaseConnected()
		{
			startup();
		}

		@Override
		public void databaseDisconnected()
		{
			shutdown();
		}

		@Override
		public void databaseCommit() { }

		@Override
		public void databaseRollback() { }
	}
}