package org.dyndns.doujindb.conf;

import java.io.*;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.conf.event.*;

/**  
* Configuration.java - Configuration Gateway.
* @author  nozomu
* @version 1.0
*/
public final class Configuration
{
	private static IConfiguration instance = new XMLConfiguration();
	private static CopyOnWriteArraySet<ConfigurationListener> listeners = new CopyOnWriteArraySet<ConfigurationListener>();
	
	protected static final File CONFIG_FILE = new File(Core.DOUJINDB_HOME, "config.xml");
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(Configuration.class);

	public static final ConfigurationItem<Font>    ui_font = new ConfigurationItem<Font>(new Font("Dialog.plain", Font.PLAIN, 11), "Default JCK font used to render Japanese/Chinese/Korean strings");
	public static final ConfigurationItem<Boolean> ui_alwaysontop = new ConfigurationItem<Boolean>(false, "Whether the user interface should be always painted on top of other windows");
	public static final ConfigurationItem<Boolean> ui_trayonexit = new ConfigurationItem<Boolean>(false, "Whether the user interface should be minimized on tray when is closed");
	public static final ConfigurationItem<Color>   ui_theme_foreground = new ConfigurationItem<Color>(new Color(0xAA, 0xAA, 0xAA), "Foreground theme color");
	public static final ConfigurationItem<Color>   ui_theme_background = new ConfigurationItem<Color>(new Color(0x22, 0x22, 0x22), "Background theme color");
	public static final ConfigurationItem<Boolean> ui_panel_book_preview = new ConfigurationItem<Boolean>(true, "Enable Book image preview");
	public static final ConfigurationItem<File>    dat_media_filestore = new ConfigurationItem<File>(new File(System.getProperty("java.io.tmpdir")), "The folder in which are stored all the media files");
	public static final ConfigurationItem<String>  dat_export_extension = new ConfigurationItem<String>(".zip", "Default file extension given to files when exporting media archives");
	public static final ConfigurationItem<Boolean> dat_cache_enable = new ConfigurationItem<Boolean>(true, "Keep a local cache of cover files");
	public static final ConfigurationItem<File>    dat_cache_filestore = new ConfigurationItem<File>(new File(System.getProperty("java.io.tmpdir")), "Local cover cache directory");
//	public static final ConfigurationItem<Integer> net_listen_port = new ConfigurationItem<Integer>(1099, "Network port used to accept incoming connections");
	public static final ConfigurationItem<Boolean> sys_check_updates = new ConfigurationItem<Boolean>(true, "Check if updates are available");
	public static final ConfigurationItem<String>  db_connection_driver = new ConfigurationItem<String>("sql.jdbc.Driver", "SQL Driver full qualified class name");
	public static final ConfigurationItem<String>  db_connection_url = new ConfigurationItem<String>("jdbc:sql://hostname/dbname", "SQL Connection URL");
	public static final ConfigurationItem<String>  db_connection_username = new ConfigurationItem<String>("", "Database username");
	public static final ConfigurationItem<String>  db_connection_password = new ConfigurationItem<String>("", "Database password");
	public static final ConfigurationItem<Integer> db_connection_timeout = new ConfigurationItem<Integer>(5, "JDBC connection timeout in seconds");
	public static final ConfigurationItem<Level>   log_org_dyndns_doujindb_conf = new ConfigurationItem<Level>(Level.INFO, "Log LEVEL for doujindb configuration");
	public static final ConfigurationItem<Level>   log_org_dyndns_doujindb_dat = new ConfigurationItem<Level>(Level.INFO, "Log LEVEL for doujindb datastore");
	public static final ConfigurationItem<Level>   log_org_dyndns_doujindb_db = new ConfigurationItem<Level>(Level.INFO, "Log LEVEL for doujindb database");
	public static final ConfigurationItem<Level>   log_org_dyndns_doujindb_plug = new ConfigurationItem<Level>(Level.INFO, "Log LEVEL for doujindb plugins");
	public static final ConfigurationItem<Level>   log_org_dyndns_doujindb_ui = new ConfigurationItem<Level>(Level.INFO, "Log LEVEL for doujindb user interface");
	public static final ConfigurationItem<Level>   log_org_dyndns_doujindb_util = new ConfigurationItem<Level>(Level.INFO, "Log LEVEL for doujindb utility");
	public static final ConfigurationItem<Level>   log_org_dyndns_doujindb_net = new ConfigurationItem<Level>(Level.INFO, "Log LEVEL for doujindb network");
	public static final ConfigurationItem<Level>   log_org_apache_cayenne = new ConfigurationItem<Level>(Level.INFO, "Log LEVEL for cayenne");
	public static final ConfigurationItem<Boolean> plugin_discovery_enable = new ConfigurationItem<Boolean>(true, "Enable automatic Plugin discovery");
	public static final ConfigurationItem<Boolean> plugin_update_enable = new ConfigurationItem<Boolean>(true, "Automatically update Plugins");
	public static final ConfigurationItem<Integer> plugin_load_timeout = new ConfigurationItem<Integer>(30, "Timeout (in seconds) which PluginManager will wait for a plugin to start");
	public static final ConfigurationItem<Integer> plugin_unload_timeout = new ConfigurationItem<Integer>(30, "Timeout (in seconds) which PluginManager will wait for a plugin to stop");

	public static Object configRead(String key) throws ConfigurationException
	{
		LOG.debug("call configRead({})", key);
		Object o = instance.configRead(key);
		LOG.debug("retn configRead({}) = {}", key, o);
		return o;
	}

	public static void configWrite(String key, Object value) throws ConfigurationException
	{
		LOG.debug("call configWrite({}, {})", key, value);
		instance.configWrite(key, value);
		for(ConfigurationListener cl : listeners)
			cl.configurationUpdated(key);
	}

	public static void configAdd(String key, String info, Object value) throws ConfigurationException
	{
		LOG.debug("call configAdd({}, {}, {})", new Object[]{ key, info, value });
		instance.configAdd(key, info, value);
		for(ConfigurationListener cl : listeners)
			cl.configurationAdded(key);
	}

	public static void configRemove(String key) throws ConfigurationException
	{
		LOG.debug("call configRemove({})", key);
		for(ConfigurationListener cl : listeners)
			cl.configurationDeleted(key);
		instance.configRemove(key);
	}

	public static boolean configExists(String key)
	{
		LOG.debug("call configExists({})", key);
		return instance.configExists(key);
	}
	
	public static String configInfo(String key)
	{
		LOG.debug("call configInfo({})", key);
		return instance.configInfo(key);
	}

	public static Iterable<String> keys()
	{
		LOG.debug("call keys()");
		return instance.keys();
	}

	public static Iterable<Object> values()
	{
		LOG.debug("call values()");
		return instance.values();
	}

	public static void configLoad() throws ConfigurationException
	{
		LOG.debug("call configLoad()");
		instance.configLoad();
	}

	public static void configSave() throws ConfigurationException
	{
		LOG.debug("call configSave()");
		instance.configSave();
	}
	
	public static void addConfigurationListener(ConfigurationListener cl)
	{
		LOG.debug("call addConfigurationListener({})", cl);
		listeners.add(cl);
	}
	
	public static void removeConfigurationListener(ConfigurationListener cl)
	{
		LOG.debug("call removeConfigurationListener({})", cl);
		listeners.remove(cl);
	}
}
