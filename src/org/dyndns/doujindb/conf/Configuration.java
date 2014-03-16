package org.dyndns.doujindb.conf;

import java.io.*;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArraySet;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.log.Logger;
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
	
	private static final String TAG = "Configuration : ";

	static
	{
		Logger.logInfo(TAG + "initializing.");
		configAdd("org.dyndns.doujindb.ui.font",               "<html><body>Default JCK font.<br/>Used to render Japanese/Chinese/Korean strings.</body></html>", new Font("Dialog.plain", Font.PLAIN, 11));
		configAdd("org.dyndns.doujindb.ui.font_size",          "<html><body>Default font size.</body></html>", 11);
		configAdd("org.dyndns.doujindb.ui.always_on_top",      "<html><body>Whether the user interface should be always painted on top of other windows.</html>", false);
		configAdd("org.dyndns.doujindb.ui.tray_on_exit",       "<html><body>Whether the user interface should be minimized on tray when is closed.</body></html>", false);
		configAdd("org.dyndns.doujindb.ui.theme.color",        "<html><body>Foreground windows color.</body></html>", new Color(0xAA, 0xAA, 0xAA));
		configAdd("org.dyndns.doujindb.ui.theme.background",   "<html><body>Background windows color.</body></html>", new Color(0x22, 0x22, 0x22));
		configAdd("org.dyndns.doujindb.dat.datastore",         "<html><body>The folder in which are stored all the media files.</body></html>", System.getProperty("java.io.tmpdir"));
		configAdd("org.dyndns.doujindb.dat.file_extension",    "<html><body>Default file extension given to files when exporting media archives.</body></html>", ".zip");
		configAdd("org.dyndns.doujindb.dat.temp",              "<html><body>Temporary folder used to store session media files.</body></html>", System.getProperty("java.io.tmpdir"));
		configAdd("org.dyndns.doujindb.net.listen_port",       "<html><body>Network port used to accept incoming connections.</body></html>", 1099);
		configAdd("org.dyndns.doujindb.net.check_updates",     "<html><body>Whether to check if program updates are available.</body></html>", false);
		configAdd("org.dyndns.doujindb.log.debug",             "<html><body>Log debug messages.</body></html>", false);
		configAdd("org.dyndns.doujindb.log.cayenne",           "<html><body>Cayenne logging.</body></html>", false);
		configAdd("org.dyndns.doujindb.db.driver",             "<html><body>SQL Driver full qualified class name.</body></html>", "sql.jdbc.Driver");
		configAdd("org.dyndns.doujindb.db.url",                "<html><body>SQL Connection URL</body></html>", "jdbc:sql://localhost/db");
		configAdd("org.dyndns.doujindb.db.username",           "<html><body>Database username.</body></html>", "");
		configAdd("org.dyndns.doujindb.db.password",           "<html><body>Database password.</body></html>", "");
		configAdd("org.dyndns.doujindb.db.connection_timeout", "<html><body>JDBC connection timeout in seconds.</body></html>", 5);
	}

	public static Object configRead(String key) throws ConfigurationException
	{
		return instance.configRead(key);
	}

	public static void configWrite(String key, Object value) throws ConfigurationException
	{
		instance.configWrite(key, value);
		for(ConfigurationListener cl : listeners)
			cl.configUpdated(key);
	}

	public static void configAdd(String key, String info, Object value) throws ConfigurationException
	{
		instance.configAdd(key, info, value);
		for(ConfigurationListener cl : listeners)
			cl.configAdded(key);
	}

	public static void configRemove(String key) throws ConfigurationException
	{
		for(ConfigurationListener cl : listeners)
			cl.configDeleted(key);
		instance.configRemove(key);
	}

	public static boolean configExists(String key)
	{
		return instance.configExists(key);
	}
	
	public static String configInfo(String key)
	{
		return instance.configInfo(key);
	}

	public static Iterable<String> keys()
	{
		return instance.keys();
	}

	public static Iterable<Object> values()
	{
		return instance.values();
	}

	public static void configLoad() throws ConfigurationException
	{
		instance.configLoad();
	}

	public static void configSave() throws ConfigurationException
	{
		instance.configSave();
	}
	
	public static void addConfigurationListener(ConfigurationListener cl)
	{
		listeners.add(cl);
	}
	
	public static void removeConfigurationListener(ConfigurationListener cl)
	{
		listeners.remove(cl);
	}
}
