package org.dyndns.doujindb.plug;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.Configuration;

/**  
* PluginManager.java - DoujinDB Plugin Manager.
* @author  nozomu
* @version 1.0
*/
public final class PluginManager
{
	private static Set<Plugin> plugins = new HashSet<Plugin>();
	
	private static final File PLUGIN_INDEX = new File(Core.DOUJINDB_HOME, "plugins.xml");

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
				serialize();
			}
		});
	}
	
	public static Iterable<Plugin> listAll()
	{
		return plugins;
	}
	
	public static void doBootstrap()
	{
		discover();
		startup();
	}
	
	private static void discover()
	{
		File libDirectory = new File(Core.DOUJINDB_HOME, "lib");
		// check if lib directory is present
		if(!libDirectory.exists())
			return;
		// check each .jar file
		for(File file : libDirectory.listFiles(new FilenameFilter()
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
				    	LOG.info("Found plugin [{}]", className);
				    	; //TODO Add file.jar to SystemClassLoader, then load Plugin
				    }
				}
			} catch (IOException ioe) {
			} catch (ClassNotFoundException cnfe) {
			}
		}
		for(String pluginName : new String[]{
				"org.dyndns.doujindb.plug.impl.mugimugi.DoujinshiDBScanner",
				"org.dyndns.doujindb.plug.impl.imagesearch.ImageSearch"
			})
			try {
				LOG.info("Found plugin [{}]", pluginName);
				Plugin plugin = (Plugin) Class.forName(pluginName).newInstance();
				plugins.add(plugin);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	private static void unserialize()
	{
		LOG.debug("call unserialize()");
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
				} catch (RuntimeException | InstantiationException | IllegalAccessException e) {
					LOG.error("Error loading plugin [{}]", pluginName, e);
				}
			}
		} catch (FileNotFoundException fnfe) {
			;
		} catch (JAXBException | ClassNotFoundException | NullPointerException e) {
			LOG.error("Error loading plugins", e);
		} finally {
			try { in.close(); } catch (Exception e) { }
		}
	}
	
	private static void serialize()
	{
		LOG.debug("call serialize()");
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
		} catch (IOException | JAXBException e) {
			LOG.error("Error saving plugins", e);
		} finally {
			try { out.close(); } catch (Exception e) { }
		}
	}
	
	private static void startup()
	{
		LOG.debug("call startup()");
		final int timeout = (Integer) Configuration.configRead("org.dyndns.doujindb.plugin.load_timeout");
		ExecutorService executor = Executors.newCachedThreadPool();
		for(final Plugin plugin : plugins)
		{
			Callable<Void> task = new Callable<Void>()
			{
				public Void call()
				{
					try {
						plugin.doStartup();
						LOG.info("Plugin [{}] started", plugin.getName());
						return null;
					} catch (PluginException pe) {
						return null;
					}
				}
			};
			Future<Void> future = executor.submit(task);
			try
			{
				future.get(timeout, TimeUnit.SECONDS);
			} catch (TimeoutException te) {
				LOG.warn("TimeoutException : Cannot startup plugin [{}]", plugin.getName(), te);
			} catch (InterruptedException ie) {
				LOG.warn("InterruptedException : Cannot startup plugin [{}]", plugin.getName(), ie);
			} catch (ExecutionException ee) {
				LOG.warn("ExecutionException : Cannot startup plugin [{}]", plugin.getName(), ee);
			} finally {
			   future.cancel(true);
			}
		}
	}
	
	private static void shutdown()
	{
		LOG.debug("call shutdown()");
		final int timeout = (Integer) Configuration.configRead("org.dyndns.doujindb.plugin.unload_timeout");
		ExecutorService executor = Executors.newCachedThreadPool();
		for(final Plugin plugin : plugins)
		{
			Callable<Void> task = new Callable<Void>()
			{
				public Void call()
				{
					try {
						plugin.doShutdown();
						LOG.info("Plugin [{}] stopped", plugin.getName());
						return null;
					} catch (PluginException pe) {
						return null;
					}
				}
			};
			Future<Void> future = executor.submit(task);
			try
			{
				future.get(timeout, TimeUnit.SECONDS);
			} catch (TimeoutException te) {
				LOG.warn("TimeoutException : Cannot shutdown plugin [{}]", plugin.getName(), te);
			} catch (InterruptedException ie) {
				LOG.warn("InterruptedException : Cannot shutdown plugin [{}]", plugin.getName(), ie);
			} catch (ExecutionException ee) {
				LOG.warn("ExecutionException : Cannot shutdown plugin [{}]", plugin.getName(), ee);
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
