package org.dyndns.doujindb.core.conf;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.conf.Properties;


public class ImplProperties implements Properties
{
	private static final long serialVersionUID = 0xDEADBEEFFEEDL;
	private HashMap<String, Serializable> values;
	private HashMap<String, String> descriptions;
	
	public ImplProperties()
	{
		values = new HashMap<String, Serializable>();
		descriptions = new HashMap<String, String>();
		values.put("org.dyndns.doujindb.ui.font", new Font("Lucida Sans", Font.PLAIN, 12));
		descriptions.put("org.dyndns.doujindb.ui.font", "<html><body>Default JCK font.<br/>Used to render Japanese/Chinese/Korean strings.</body></html>");
		values.put("org.dyndns.doujindb.ui.font_size", 11);
		descriptions.put("org.dyndns.doujindb.ui.font_size", "<html><body>Default font size.</body></html>");
		values.put("org.dyndns.doujindb.ui.delay_threads", 20);
		descriptions.put("org.dyndns.doujindb.ui.delay_threads", "<html><body>Delay used in multi-threader operation.<br/>Value is in milliseconds.</body></html>");
		values.put("org.dyndns.doujindb.ui.always_on_top", false);
		descriptions.put("org.dyndns.doujindb.ui.always_on_top", "<html><body>Whether the user interface should be always painted on top of other windows.</body></html>");
		values.put("org.dyndns.doujindb.ui.tray_on_exit", false);
		descriptions.put("org.dyndns.doujindb.ui.tray_on_exit", "<html><body>Whether the user interface should be minimized on tray when is closed.</body></html>");
		values.put("org.dyndns.doujindb.ui.theme.color", new Color(0xAA, 0xAA, 0xAA));
		descriptions.put("org.dyndns.doujindb.ui.theme.color", "<html><body>Foreground windows color.</body></html>");
		values.put("org.dyndns.doujindb.ui.theme.background", new Color(0x22, 0x22, 0x22));
		descriptions.put("org.dyndns.doujindb.ui.theme.background", "<html><body>Background windows color.</body></html>");
		values.put("org.dyndns.doujindb.dat.datastore", new File(System.getProperty("java.io.tmpdir")));
		descriptions.put("org.dyndns.doujindb.dat.datastore", "<html><body>The folder in which are stored all the media files.</body></html>");
		values.put("org.dyndns.doujindb.dat.file_extension", ".douz");
		descriptions.put("org.dyndns.doujindb.dat.file_extension", "<html><body>Default file extension given to files when exporting media archives.</body></html>");
		values.put("org.dyndns.doujindb.dat.temp", new File(System.getProperty("java.io.tmpdir")));
		descriptions.put("org.dyndns.doujindb.dat.temp", "<html><body>Temporary folder used to store session media files.</body></html>");
		values.put("org.dyndns.doujindb.dat.save_on_exit", false);
		descriptions.put("org.dyndns.doujindb.dat.save_on_exit", "<html><body>Whether the database should be saved on exit.</body></html>");
		values.put("org.dyndns.doujindb.dat.auto_commit", false);
		descriptions.put("org.dyndns.doujindb.dat.auto_commit", "<html><body>Whether the database should auto-commit on every operation.</body></html>");
		//values.put("org.dyndns.doujindb.dat.replace_on_import", false);
		//values.put("org.dyndns.doujindb.dat.replace_on_import", "<html><body></body></html>");
		values.put("org.dyndns.doujindb.net.autocheck_updates", false);
		descriptions.put("org.dyndns.doujindb.net.autocheck_updates", "<html><body>Whether to check if program updates are available.</body></html>");
		values.put("org.dyndns.doujindb.net.listen_port", 8899);
		descriptions.put("org.dyndns.doujindb.net.listen_port", "<html><body>Network port used to accept incoming connections.</body></html>");
		values.put("org.dyndns.doujindb.net.connect_on_start", false);
		descriptions.put("org.dyndns.doujindb.net.connect_on_start", "<html><body>Whether to connect on program startup.</body></html>");
	}
	
	public Serializable getValue(String key) throws PropertyException
	{
		if(!values.containsKey(key))
			throw new PropertyException("Invalid key '" + key + "'");
		return values.get(key);
	}
	
	public void setValue(String key, Serializable value) throws PropertyException
	{
		if(!values.containsKey(key))
			throw new PropertyException("Invalid key '" + key + "'");
		values.put(key, value);
	}
	
	public void newValue(String key, Serializable value) throws PropertyException
	{
		if(values.containsKey(key))
			throw new PropertyException("Key '" + key + "' is already present");
		values.put(key, value);
		descriptions.put(key, "");
	}
	
	public Iterable<String> values()
	{
		return values.keySet();
	}
	
	public boolean containsValue(String key)
	{
		return values.containsKey(key);
	}
	
	public Serializable getDescription(String key) throws PropertyException
	{
		if(!descriptions.containsKey(key))
			throw new PropertyException("Invalid key '" + key + "'");
		return descriptions.get(key);
	}
	
	public void setDescription(String key, String value) throws PropertyException
	{
		if(!descriptions.containsKey(key))
			throw new PropertyException("Invalid key '" + key + "'");
		descriptions.put(key, value);
	}
}
