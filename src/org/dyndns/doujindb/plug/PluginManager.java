package org.dyndns.doujindb.plug;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.Configuration;
import org.dyndns.doujindb.plug.event.PluginListener;

/**  
* PluginManager.java - DoujinDB Plugin Manager.
* @author  nozomu
* @version 1.0
*/
public final class PluginManager
{
	private static Set<Plugin> plugins = new HashSet<Plugin>();
	
	private static CopyOnWriteArraySet<PluginListener> listeners = new CopyOnWriteArraySet<PluginListener>();
	
	static final File PLUGIN_HOME = new File(Core.DOUJINDB_HOME, "plugin");

	private static final Logger LOG = (Logger) LoggerFactory.getLogger(PluginManager.class);
	
	private PluginManager() { }
	
	static
	{
		/*
		 * Register a shutdown hook to handle the shutdown of this JVM for every Plugin
		 * If the Plugin doesn't shutdown after PLUGIN_TIMEOUT (seconds), skip it
		 */
		LOG.info("Registering shutdown hook for this JVM");
		Runtime.getRuntime().addShutdownHook(new Thread("pluginmanager-shutdownhook")
		{
			@Override
			public void run()
			{
				shutdown();
			}
		});
	}
	
	public static void doBootstrap()
	{
		discover();
		startup();
	}
	
	private static void discover()
	{
		LOG.debug("call discover()");
		// Search for .jar files in PLUGIN_HOME directory
		for(File file : PLUGIN_HOME.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		}))
		{
			LOG.debug("scanning file {} ...", file.getName());
			try {
				// Open .jar file as JarFile
				JarFile jf = new JarFile(file);
				// Search 'Main-Class' manifest attribute
				if(jf.getManifest().getMainAttributes().getValue("Main-Class") != null) {
				    String className = jf.getManifest().getMainAttributes().getValue("Main-Class");
				    Set<String> classes = new HashSet<String>();
				    // Create a new ClassLoader from the .jar file
				    URLClassLoader child = new URLClassLoader(new URL[]{file.toURI().toURL()}, PluginManager.class.getClassLoader());
				    try {
				    	// List 'Main-Class' public classes
				    	for(Class<?> clazz : Class.forName(className, false, child).getClasses()) {
				    		classes.add(clazz.getCanonicalName());
				    	}
					} catch (ClassNotFoundException cnfe) {
						LOG.error("IOException while inspecting class {}", className, cnfe);
					}
				    // Check if Plugin.class is implemented
				    if(classes.contains(Plugin.class.getCanonicalName())) {
				    	LOG.info("Found plugin [{}]", className);
				    	; //TODO Add file.jar to SystemClassLoader, then load Plugin
				    }
				}
				jf.close();
			} catch (IOException ioe) {
				LOG.error("IOException while scanning jar file {}", file.getName(), ioe);
			}
		}
		for(String pluginName : new String[]{
				"org.dyndns.doujindb.plug.impl.mugimugi.DoujinshiDBScanner",
				"org.dyndns.doujindb.plug.impl.imagesearch.ImageSearch"
			})
			try {
				LOG.info("Found plugin [{}]", pluginName);
				Plugin plugin = (Plugin) Class.forName(pluginName).newInstance();
				install(plugin);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	private static void startup()
	{
		LOG.debug("call startup()");
		final int timeout = Configuration.plugin_load_timeout.get();
		for(final Plugin plugin : plugins)
		{
			Callable<Void> task = new Callable<Void>()
			{
				public Void call() throws Exception
				{
					try {
						plugin.doStartup();
						firePluginStarted(plugin);
						LOG.info("Plugin [{}] started", plugin.getNamespace());
						return null;
					} catch (PluginException pe) {
						pe.printStackTrace();
						return null;
					}
				}
			};
			FutureTask<Void> future = new FutureTask<Void>(task);
			try
			{
				new Thread(future, "pluginmanager-startup-plugin").start();
				future.get(timeout, TimeUnit.SECONDS);
			} catch (TimeoutException te) {
				LOG.warn("TimeoutException : Cannot startup plugin [{}]", plugin.getNamespace(), te);
			} catch (InterruptedException ie) {
				LOG.warn("InterruptedException : Cannot startup plugin [{}]", plugin.getNamespace(), ie);
			} catch (ExecutionException ee) {
				LOG.warn("ExecutionException : Cannot startup plugin [{}]", plugin.getNamespace(), ee);
			} finally {
			   future.cancel(true);
			}
		}
	}
	
	private static void shutdown()
	{
		LOG.debug("call shutdown()");
		final int timeout = Configuration.plugin_unload_timeout.get();
		for(final Plugin plugin : plugins)
		{
			Callable<Void> task = new Callable<Void>()
			{
				public Void call()
				{
					try {
						plugin.doShutdown();
						firePluginStopped(plugin);
						LOG.info("Plugin [{}] stopped", plugin.getNamespace());
						return null;
					} catch (PluginException pe) {
						return null;
					}
				}
			};
			FutureTask<Void> future = new FutureTask<Void>(task);
			try
			{
				new Thread(future, "pluginmanager-shutdown-plugin").start();
				future.get(timeout, TimeUnit.SECONDS);
			} catch (TimeoutException te) {
				LOG.warn("TimeoutException : Cannot shutdown plugin [{}]", plugin.getNamespace(), te);
			} catch (InterruptedException ie) {
				LOG.warn("InterruptedException : Cannot shutdown plugin [{}]", plugin.getNamespace(), ie);
			} catch (ExecutionException ee) {
				LOG.warn("ExecutionException : Cannot shutdown plugin [{}]", plugin.getNamespace(), ee);
			} finally {
			   future.cancel(true);
			}
		}
	}
	
	private static void install(Plugin plugin) throws PluginException
	{
		plugin.doInstall();
		firePluginInstalled(plugin);
		plugins.add(plugin);
	}
	
	public static void addPluginListener(PluginListener pl)
	{
		LOG.debug("call addPluginListener({})", pl);
		listeners.add(pl);
	}
	
	public static void removePluginListener(PluginListener pl)
	{
		LOG.debug("call removePluginListener({})", pl);
		listeners.remove(pl);
	}
	
	static void firePluginInstalled(Plugin plugin)
	{
		LOG.debug("call firePluginInstalled({})", plugin);
		for(PluginListener pl : listeners)
			pl.pluginInstalled(plugin);
	}
	
	static void firePluginUninstalled(Plugin plugin)
	{
		LOG.debug("call firePluginUninstalled({})", plugin);
		for(PluginListener pl : listeners)
			pl.pluginUninstalled(plugin);
	}
	
	static void firePluginStarted(Plugin plugin)
	{
		LOG.debug("call firePluginStarted({})", plugin);
		for(PluginListener pl : listeners)
			pl.pluginStarted(plugin);
	}
	
	static void firePluginStopped(Plugin plugin)
	{
		LOG.debug("call firePluginStopped({})", plugin);
		for(PluginListener pl : listeners)
			pl.pluginStopped(plugin);
	}
	
	static void firePluginUpdated(Plugin plugin)
	{
		LOG.debug("call firePluginUpdated({})", plugin);
		for(PluginListener pl : listeners)
			pl.pluginUpdated(plugin);
	}
}
