package org.dyndns.doujindb.plug;

import java.io.*;
import java.net.MalformedURLException;
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
	static final File PLUGIN_LIBS = new File(Core.DOUJINDB_HOME, "lib");
	
	private static final PluginClassLoader mClassLoader = new PluginClassLoader();

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
		for(File file : PLUGIN_LIBS.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		})) {
			LOG.debug("Scanning file {} ...", file.getName());
			try {
				// Open .jar file as JarFile
				JarFile jf = new JarFile(file);
				// Search 'Main-Class' manifest attribute
				String mainClass = jf.getManifest().getMainAttributes().getValue("X-Plugin-Class");
				if(mainClass != null) {
					// Add .jar file to PluginManager ClassLoader
					LOG.info("Adding file {} to ClassPath ...", file);
					mClassLoader.addJar(file);
					try {
						// Load Main-Class from PluginManager ClassLoader
						Class<?> clazz = Class.forName(mainClass, false, mClassLoader);
						// Check if Plugin.class is implemented
						if(clazz.getSuperclass().equals(Plugin.class)) {
							LOG.info("Found plugin [{}]", mainClass);
							{
								String classPath = jf.getManifest().getMainAttributes().getValue("X-Plugin-Class-Path");
								if(classPath != null) {
									StringTokenizer st = new StringTokenizer(classPath);
									while(st.hasMoreTokens()) {
										String libraryName = st.nextToken();
										File libraryFile = new File(PLUGIN_LIBS, libraryName);
										// Add library .jar file to ClassLoader
										LOG.info("Adding library {} to ClassPath ...", libraryFile);
										mClassLoader.addJar(libraryFile);
									}
								}
							}
							Plugin plugin = (Plugin) clazz.newInstance();
							try {
								install(plugin);
							} catch (PluginException pe) {
								LOG.error("Error loading plugin [{}]", mainClass, pe);
							}
						}
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						LOG.error("Error inspecting class {}", mainClass, e);
					}
				}
				jf.close();
			} catch (IOException ioe) {
				LOG.error("Error scanning jar file {}", file.getName(), ioe);
			}
		}
		for(String pluginName : new String[]{
				"org.dyndns.doujindb.plug.impl.dataimport.DataImport",
				"org.dyndns.doujindb.plug.impl.imagesearch.ImageSearch"
			})
			try {
				LOG.info("Found plugin [{}]", pluginName);
				Plugin plugin = (Plugin) Class.forName(pluginName).newInstance();
				install(plugin);
			} catch (Error | Exception e) {
				LOG.error("Error loading plugin [{}]", pluginName, e);
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
						LOG.debug("Starting plugin [{}]", plugin);
						firePluginStarted(plugin);
						LOG.info("Started plugin [{}]", plugin);
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
			} catch (TimeoutException | InterruptedException | ExecutionException e) {
				LOG.warn("Error starting plugin [{}]", plugin, e);
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
						LOG.debug("Stopping plugin [{}]", plugin);
						plugin.doShutdown();
						firePluginStopped(plugin);
						LOG.info("Stopped plugin [{}]", plugin);
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
			} catch (TimeoutException | InterruptedException | ExecutionException e) { 
				LOG.warn("Error stopping plugin [{}]", plugin, e);
			} finally {
			   future.cancel(true);
			}
		}
	}
	
	private static void install(Plugin plugin) throws PluginException
	{
		LOG.debug("call install({})", plugin);
		plugin.doInstall();
		firePluginInstalled(plugin);
		plugins.add(plugin);
	}
	
	public static void addPluginListener(PluginListener listener)
	{
		LOG.debug("call addPluginListener({})", listener);
		listeners.add(listener);
	}
	
	public static void removePluginListener(PluginListener listener)
	{
		LOG.debug("call removePluginListener({})", listener);
		listeners.remove(listener);
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
	
	private static final class PluginClassLoader extends URLClassLoader
	{
		public PluginClassLoader() {
			super(new URL[]{});
		}
		public void addJar(File jarFile) throws MalformedURLException {
			super.addURL(jarFile.toURI().toURL());
		}
	}
}
