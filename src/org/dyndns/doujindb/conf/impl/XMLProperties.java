package org.dyndns.doujindb.conf.impl;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.xml.bind.annotation.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.conf.Properties;

final class XMLProperties implements Properties
{
	private HashMap<String, Property> values;
	
	XMLProperties()
	{
		values = new HashMap<String, Property>();
		{
			Property prop = new PropertyImpl();
			prop.setValue(new Font("Dialog.plain", Font.PLAIN, 11));
			prop.setDescription("<html><body>Default JCK font.<br/>Used to render Japanese/Chinese/Korean strings.</body></html>");
			values.put("org.dyndns.doujindb.ui.font", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(11);
			prop.setDescription("<html><body>Default font size.</body></html>");
			values.put("org.dyndns.doujindb.ui.font_size", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(false);
			prop.setDescription("<html><body>Whether the user interface should be always painted on top of other windows.</html>");
			values.put("org.dyndns.doujindb.ui.always_on_top", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(false);
			prop.setDescription("<html><body>Whether the user interface should be minimized on tray when is closed.</body></html>");
			values.put("org.dyndns.doujindb.ui.tray_on_exit", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(new Color(0xAA, 0xAA, 0xAA));
			prop.setDescription("<html><body>Foreground windows color.</body></html>");
			values.put("org.dyndns.doujindb.ui.theme.color", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(new Color(0x22, 0x22, 0x22));
			prop.setDescription("<html><body>Background windows color.</body></html>");
			values.put("org.dyndns.doujindb.ui.theme.background", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(System.getProperty("java.io.tmpdir"));
			prop.setDescription("<html><body>The folder in which are stored all the media files.</body></html>");
			values.put("org.dyndns.doujindb.dat.datastore", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(".douz");
			prop.setDescription("<html><body>Default file extension given to files when exporting media archives.</body></html>");
			values.put("org.dyndns.doujindb.dat.file_extension", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(System.getProperty("java.io.tmpdir"));
			prop.setDescription("<html><body>Temporary folder used to store session media files.</body></html>");
			values.put("org.dyndns.doujindb.dat.temp", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(1099);
			prop.setDescription("<html><body>Network port used to accept incoming connections.</body></html>");
			values.put("org.dyndns.doujindb.net.listen_port", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(false);
			prop.setDescription("<html><body>Whether to check if program updates are available.</body></html>");
			values.put("org.dyndns.doujindb.net.autocheck_updates", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(true);
			prop.setDescription("<html><body>Logs messages.</body></html>");
			values.put("org.dyndns.doujindb.log.info", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(true);
			prop.setDescription("<html><body>Logs warnings.</body></html>");
			values.put("org.dyndns.doujindb.log.warning", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(true);
			prop.setDescription("<html><body>Logs errors.</body></html>");
			values.put("org.dyndns.doujindb.log.error", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(false);
			prop.setDescription("<html><body>Cayenne logging.</body></html>");
			values.put("org.dyndns.doujindb.log.cayenne", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue("sql.jdbc.Driver");
			prop.setDescription("<html><body>SQL Driver full qualified class name.</body></html>");
			values.put("org.dyndns.doujindb.db.driver", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue("jdbc:sql://localhost/db");
			prop.setDescription("<html><body>SQL Connection URL</body></html>");
			values.put("org.dyndns.doujindb.db.url", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue("admin");
			prop.setDescription("<html><body>Database username.</body></html>");
			values.put("org.dyndns.doujindb.db.username", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue("");
			prop.setDescription("<html><body>Database password.</body></html>");
			values.put("org.dyndns.doujindb.db.password", prop);
		}
		{
			Property prop = new PropertyImpl();
			prop.setValue(5);
			prop.setDescription("<html><body>JDBC connection timeout.</body></html>");
			values.put("org.dyndns.doujindb.db.connection_timeout", prop);
		}
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
		values.put(key, new PropertyImpl());
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

	@Override
	public synchronized void load() throws PropertyException
	{
		File file = new File(System.getProperty("doujindb.home"), "config.xml");
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(file);
			JAXBContext context = JAXBContext.newInstance(XMLPropertyList.class);
			Unmarshaller um = context.createUnmarshaller();
			XMLPropertyList xmlprops = (XMLPropertyList) um.unmarshal(in);
			for(XMLProperty xmlprop : xmlprops.properties)
			{
				Property prop = new PropertyImpl();
				if(values.containsKey(xmlprop.key))
					prop.setDescription(values.get(xmlprop.key).getDescription());
				if(xmlprop.type.equals("Boolean"))
				{
					Boolean value = Boolean.parseBoolean(xmlprop.value);
					prop.setValue(value);
				}
				if(xmlprop.type.equals("Number"))
				{
					Integer value = Integer.parseInt(xmlprop.value);
					prop.setValue(value);
				}
				if(xmlprop.type.equals("String"))
				{
					String value = xmlprop.value;
					prop.setValue(value);
				}
				if(xmlprop.type.equals("Font"))
				{
					Font value = new Font(xmlprop.value, Font.PLAIN, 12);
					prop.setValue(value);
				}
				if(xmlprop.type.equals("Color"))
				{
					String[] values = xmlprop.value.split(",");
					Color value = new Color(Integer.parseInt(values[0]),
											Integer.parseInt(values[1]),
											Integer.parseInt(values[2]),
											Integer.parseInt(values[3]));
					prop.setValue(value);
				}
				values.put(xmlprop.key, prop);
			}
		} catch (NullPointerException npe) {
			try { in.close(); } catch (Exception e) { }
			throw new PropertyException(npe);
		} catch (JAXBException jaxbe) {
			try { in.close(); } catch (Exception e) { }
			throw new PropertyException(jaxbe);
		} catch (FileNotFoundException fnfe) {
			throw new PropertyException(fnfe);
		} catch (Exception e) {
			throw new PropertyException(e);
		}
		try { in.close(); } catch (Exception e) { }
	}

	@Override
	public synchronized void save() throws PropertyException
	{
		File file = new File(System.getProperty("doujindb.home"), "config.xml");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(file);
			JAXBContext context = JAXBContext.newInstance(XMLPropertyList.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			XMLPropertyList xmlprops = new XMLPropertyList();
			{
				for(String key : values.keySet())
				{
					Property prop = values.get(key);
					XMLProperty xmlprop = new XMLProperty();
					xmlprop.key = key;
					if(prop.getValue() instanceof Boolean)
					{
						xmlprop.value = "" + ((Boolean) prop.getValue()) + "";
						xmlprop.type = "Boolean";
					}
					if(prop.getValue() instanceof Integer)
					{
						xmlprop.value = "" + ((Integer) prop.getValue()) + "";
						xmlprop.type = "Number";
					}
					if(prop.getValue() instanceof String)
					{
						xmlprop.value = "" + ((String) prop.getValue()) + "";
						xmlprop.type = "String";
					}
					if(prop.getValue() instanceof Font)
					{
						xmlprop.value = "" + ((Font) prop.getValue()).getName() + "";
						xmlprop.type = "Font";
					}
					if(prop.getValue() instanceof Color)
					{
						Color value = (Color) prop.getValue();
						xmlprop.value = "" + value.getRed() + "," + 
											value.getGreen() + "," + 
											value.getBlue() + "," +
											value.getAlpha() + "";
						xmlprop.type = "Color";
					}
					xmlprops.properties.add(xmlprop);
				}
			}
			m.marshal(xmlprops, out);
		} catch (NullPointerException npe) {
			try { out.close(); } catch (Exception e) { }
			throw new PropertyException(npe);
		} catch (JAXBException jaxbe) {
			try { out.close(); } catch (Exception e) { }
			throw new PropertyException(jaxbe);
		} catch (FileNotFoundException fnfe) {
			throw new PropertyException(fnfe);
		}
		try { out.close(); } catch (Exception e) { }
	}
	
	@XmlRootElement(namespace = "org.dyndns.doujindb.conf", name="Properties")
	private static final class XMLPropertyList
	{
		@XmlElements({
		    @XmlElement(name="Property", type=XMLProperty.class)
		  })
		private List<XMLProperty> properties = new Vector<XMLProperty>();
	}
	
	@XmlRootElement(namespace = "org.dyndns.doujindb.conf", name="Property")
	private static final class XMLProperty
	{
		@XmlAttribute(name="Key", required=true)
		private String key;
		@XmlAttribute(name="Type", required=true)
		private String type;
		@XmlAttribute(name="Value")
		private String value;
	}
}
