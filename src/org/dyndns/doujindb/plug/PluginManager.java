package org.dyndns.doujindb.plug;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.*;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.log.*;

/**  
* PluginManager.java - DoujinDB Plugin Manager.
* @author  nozomu
* @version 1.0
*/
public final class PluginManager
{
	private static Set<Plugin> plugins = new HashSet<Plugin>();
	
	private static int PLUGIN_TIMEOUT = 60;
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
				stopAll();
				saveAll();
			}
		});
	}
	
	public static void install(Plugin plugin) throws PluginException
	{
		if(plugins.contains(plugin))
			throw new PluginException("Plugin '" + plugin.getName() + "' is already installed.");
		plugin.install();
		plugins.add(plugin);
		saveAll();
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
		saveAll();
	}
	
	public static Iterable<Plugin> listAll()
	{
		return plugins;
	}
	
	private static void discoverAll()
	{
		Logger.logInfo(TAG + "discovering plugins ...");
		for(File file : new File(Core.DOUJINDB_HOME, "lib").listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		}))
		{
			JarFile jf;
			try {
				jf = new JarFile(file);
				if(jf.getManifest().getMainAttributes().getValue("Main-Class") != null) {
				    String className = jf.getManifest().getMainAttributes().getValue("Main-Class");
				    Set<String> classes = new HashSet<String>();
				    for(Class<?> clazz : Class.forName(className).getClasses())
				    {
				    	classes.add(clazz.getCanonicalName());
				    }
				    if(classes.contains(Plugin.class.getCanonicalName()))
				    {
				    	Logger.logInfo(TAG + "found '" + className + "'.");
				    	; //TODO Add file.jar to SystemClassLoader, then load Plugin
				    }
				}
			} catch (IOException ioe) {
			} catch (ClassNotFoundException cnfe) {
			}
		}
		for(String pluginName : new String[]{
				"org.dyndns.doujindb.plug.impl.mugimugi.DoujinshiDBScanner"
			})
			try {
				Logger.logInfo(TAG + "found '" + pluginName + "'.");
				Plugin plugin = (Plugin) Class.forName(pluginName).newInstance();
				plugins.add(plugin);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public static void loadAll()
	{
		Logger.logInfo(TAG + "loading plugins ...");
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(PLUGIN_INDEX);
			JAXBContext context = JAXBContext.newInstance(XMLPluginManager.class);
			Unmarshaller um = context.createUnmarshaller();
			XMLPluginManager xmlroot = (XMLPluginManager) um.unmarshal(in);
			for(XMLPlugin xmlnode : xmlroot.nodes)
			{
				String pluginName = xmlnode.namespace;
				try {
					if(xmlnode.enabled)
						plugins.add((Plugin) Class.forName(pluginName).newInstance());
				} catch (RuntimeException re) {
					Logger.logError(TAG + "failed to load plugin '" + pluginName + "' : " + re.getMessage(), re);
				} catch (InstantiationException ie) {
					Logger.logError(TAG + "failed to load plugin '" + pluginName + "' : " + ie.getMessage(), ie);
				} catch (IllegalAccessException iae) {
					Logger.logError(TAG + "failed to load plugin '" + pluginName + "' : " + iae.getMessage(), iae);
				}
			}
		} catch (FileNotFoundException fnfe) {
			;
		} catch (NullPointerException npe) {
			Logger.logError(TAG + "failed to load plugins : " + npe.getMessage(), npe);
		} catch (JAXBException jaxbe) {
			Logger.logError(TAG + "failed to load plugins : " + jaxbe.getMessage(), jaxbe);
		} catch (ClassNotFoundException cnfe) {
			Logger.logError(TAG + "failed to load plugins : " + cnfe.getMessage(), cnfe);
		} finally {
			try { in.close(); } catch (Exception e) { }
		}
		
		discoverAll();
	}
	
	public static void saveAll()
	{
		Logger.logInfo(TAG + "saving plugins ...");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(PLUGIN_INDEX);
			JAXBContext context = JAXBContext.newInstance(XMLPluginManager.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			XMLPluginManager xmlroot = new XMLPluginManager();
			for(Plugin plugin : plugins)
			{
				XMLPlugin xmlnode = new XMLPlugin();
				xmlnode.namespace = plugin.getClass().getCanonicalName();
				xmlnode.enabled = true;
				xmlroot.nodes.add(xmlnode);
			}
			m.marshal(xmlroot, out);
		} catch (IOException ioe) {
			Logger.logError(TAG + "failed to save plugins : " + ioe.getMessage(), ioe);
		} catch (JAXBException jaxbe) {
			Logger.logError(TAG + "failed to save plugins : " + jaxbe.getMessage(), jaxbe);
		} finally {
			try { out.close(); } catch (Exception e) { }
		}
	}
	
	public static void startAll()
	{
		Logger.logInfo(TAG + "starting all plugins ...");
		ExecutorService executor = Executors.newCachedThreadPool();
		for(final Plugin plugin : plugins)
		{
			Callable<Void> task = new Callable<Void>()
			{
				public Void call()
				{
					try {
						plugin.startup();
						Logger.logInfo(TAG + "plugin '" + plugin.getName() + "' started");
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
				Logger.logWarning(TAG + "TimeoutException : Cannot startup plugin '" + plugin.getName() + "'", te);
			} catch (InterruptedException ie) {
				Logger.logWarning(TAG + "InterruptedException : Cannot startup plugin '" + plugin.getName() + "'", ie);
			} catch (ExecutionException ee) {
				Logger.logWarning(TAG + "ExecutionException : Cannot startup plugin '" + plugin.getName() + "'", ee);
			} finally {
			   future.cancel(true);
			}
		}
	}
	
	public static void stopAll()
	{
		Logger.logInfo(TAG + "stopping all plugins ...");
		ExecutorService executor = Executors.newCachedThreadPool();
		for(final Plugin plugin : plugins)
		{
			Callable<Void> task = new Callable<Void>()
			{
				public Void call()
				{
					try {
						plugin.shutdown();
						Logger.logInfo(TAG + "plugin '" + plugin.getName() + "' stopped");
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
				Logger.logWarning(TAG + "TimeoutException : Cannot shutdown plugin '" + plugin.getName() + "'", te);
			} catch (InterruptedException ie) {
				Logger.logWarning(TAG + "InterruptedException : Cannot shutdown plugin '" + plugin.getName() + "'", ie);
			} catch (ExecutionException ee) {
				Logger.logWarning(TAG + "ExecutionException : Cannot shutdown plugin '" + plugin.getName() + "'", ee);
			} finally {
			   future.cancel(true);
			}
		}
	}
	
	@XmlRootElement(namespace = "org.dyndns.doujindb.plug", name="PluginManager")
	private static final class XMLPluginManager
	{
		@XmlElements({
		    @XmlElement(name="Plugin", type=XMLPlugin.class)
		  })
		private List<XMLPlugin> nodes = new Vector<XMLPlugin>();
	}
	
	@XmlRootElement(namespace = "org.dyndns.doujindb.plug", name="Plugin")
	private static final class XMLPlugin
	{
		@XmlAttribute(name="Namespace", required=true)
		private String namespace;
		@XmlAttribute(name="Enabled", required=true)
		private boolean enabled;
	}
}
