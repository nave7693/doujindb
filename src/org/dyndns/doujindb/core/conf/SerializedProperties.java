package org.dyndns.doujindb.core.conf;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.conf.Properties;

public class SerializedProperties implements Properties, Serializable
{
	private static final long serialVersionUID = 0x001L;
	private HashMap<String, Property> values;
	
	public SerializedProperties()
	{
		values = new HashMap<String, Property>();
		{
			Property prop = new ImplProperty();
			prop.setValue(new Font("Lucida Sans", Font.PLAIN, 12));
			prop.setDescription("<html><body>Default JCK font.<br/>Used to render Japanese/Chinese/Korean strings.</body></html>");
			values.put("org.dyndns.doujindb.ui.font", prop);			
		}
		{
			Property prop = new ImplProperty();
			prop.setValue(11);
			prop.setDescription("<html><body>Default font size.</body></html>");
			values.put("org.dyndns.doujindb.ui.font_size", prop);			
		}
		{
			Property prop = new ImplProperty();
			prop.setValue(20);
			prop.setDescription("<html><body>Delay used in multi-threader operation.<br/>Value is in milliseconds.</body></html>");
			values.put("org.dyndns.doujindb.ui.delay_threads", prop);			
		}
		{
			Property prop = new ImplProperty();
			prop.setValue(false);
			prop.setDescription("<html><body>Whether the user interface should be always painted on top of other windows.</html>");
			values.put("org.dyndns.doujindb.ui.always_on_top", prop);			
		}
		{
			Property prop = new ImplProperty();
			prop.setValue(false);
			prop.setDescription("<html><body>Whether the user interface should be minimized on tray when is closed.</body></html>");
			values.put("org.dyndns.doujindb.ui.tray_on_exit", prop);			
		}
		{
			Property prop = new ImplProperty();
			prop.setValue(new Color(0xAA, 0xAA, 0xAA));
			prop.setDescription("<html><body>Foreground windows color.</body></html>");
			values.put("org.dyndns.doujindb.ui.theme.color", prop);			
		}
		{
			Property prop = new ImplProperty();
			prop.setValue(new Color(0x22, 0x22, 0x22));
			prop.setDescription("<html><body>Background windows color.</body></html>");
			values.put("org.dyndns.doujindb.ui.theme.background", prop);			
		}
		{
			Property prop = new ImplProperty();
			prop.setValue(System.getProperty("java.io.tmpdir"));
			prop.setDescription("<html><body>The folder in which are stored all the media files.</body></html>");
			values.put("org.dyndns.doujindb.dat.datastore", prop);			
		}
		{
			Property prop = new ImplProperty();
			prop.setValue(".douz");
			prop.setDescription("<html><body>Default file extension given to files when exporting media archives.</body></html>");
			values.put("org.dyndns.doujindb.dat.file_extension", prop);			
		}
		{
			Property prop = new ImplProperty();
			prop.setValue(System.getProperty("java.io.tmpdir"));
			prop.setDescription("<html><body>Temporary folder used to store session media files.</body></html>");
			values.put("org.dyndns.doujindb.dat.temp", prop);			
		}
		//values.put("org.dyndns.doujindb.dat.save_on_exit", false);
		//descriptions.put("org.dyndns.doujindb.dat.save_on_exit", "<html><body>Whether the database should be saved on exit.</body></html>");
		//values.put("org.dyndns.doujindb.dat.export_filename", false);
		//descriptions.put("org.dyndns.doujindb.dat.export_filename", "<html><body>The name given to exported files.</body></html>");
		//values.put("org.dyndns.doujindb.net.autocheck_updates", false);
		//descriptions.put("org.dyndns.doujindb.net.autocheck_updates", "<html><body>Whether to check if program updates are available.</body></html>");
		//values.put("org.dyndns.doujindb.net.listen_port", 8899);
		//descriptions.put("org.dyndns.doujindb.net.listen_port", "<html><body>Network port used to accept incoming connections.</body></html>");
		//values.put("org.dyndns.doujindb.net.connect_on_start", false);
		//descriptions.put("org.dyndns.doujindb.net.connect_on_start", "<html><body>Whether to connect on program startup.</body></html>");
	}
	
	public Iterable<String> keys()
	{
		return values.keySet();
	}
	
	public Iterable<Property> values()
	{
		return values.values();
	}
	
	@Override
	public void add(String key) throws PropertyException
	{
		if(values.containsKey(key))
			throw new PropertyException("Key '" + key + "' is already present.");
		values.put(key, new ImplProperty());
	}

	@Override
	public void remove(String key) throws PropertyException
	{
		if(!values.containsKey(key))
			throw new PropertyException("Invalid key '" + key + "'.");
		values.remove(key);
	}

	@Override
	public boolean contains(String key)
	{
		return values.containsKey(key);
	}

	@Override
	public Property get(String key) throws PropertyException
	{
		if(!values.containsKey(key))
			throw new PropertyException("Invalid key '" + key + "'.");
		return values.get(key);
	}
}
