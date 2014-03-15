package org.dyndns.doujindb.conf;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

import org.dyndns.doujindb.log.*;

final class XMLConfiguration implements IConfiguration
{
	private HashMap<String, Object> values;
	private HashMap<String, String> infos;
	
	private static final String TAG = "XMLConfiguration : ";
	
	public XMLConfiguration()
	{
		values = new HashMap<String, Object>();
		infos = new HashMap<String, String>();
	}
	
	@Override
	public Iterable<String> keys()
	{
		return values.keySet();
	}
	
	@Override
	public Iterable<Object> values()
	{
		return values.values();
	}
	
	@Override
	public void configAdd(String key, String info, Object value) throws ConfigurationException
	{
		if(values.containsKey(key))
			throw new ConfigurationException("Cannot add key: '" + key + "': is already present.");
		values.put(key, value);
		infos.put(key, info);
	}

	@Override
	public void configRemove(String key) throws ConfigurationException
	{
		if(!values.containsKey(key))
			throw new ConfigurationException("Cannot remove key: '" + key + "': not found.");
		values.remove(key);
		infos.remove(key);
	}

	@Override
	public boolean configExists(String key)
	{
		return values.containsKey(key);
	}

	@Override
	public Object configRead(String key) throws ConfigurationException
	{
		if(!values.containsKey(key))
			throw new ConfigurationException("Cannot read key: '" + key + "': not found.");
		return values.get(key);
	}
	
	@Override
	public void configWrite(String key, Object value) throws ConfigurationException
	{
		if(!values.containsKey(key))
			throw new ConfigurationException("Cannot write key: '" + key + "': not found.");
		if(!values.get(key).getClass().equals(value.getClass()))
			throw new ConfigurationException("Cannot write key: '" + key + "': invalid data type '" + value.getClass().getCanonicalName() + "'");
		values.put(key, value);
	}
	
	@Override
	public String configInfo(String key) throws ConfigurationException
	{
		if(!infos.containsKey(key))
			throw new ConfigurationException("Cannot get info on key: '" + key + "': not found.");
		return infos.get(key);
	}

	@Override
	public synchronized void configLoad() throws ConfigurationException
	{
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(Configuration.CONFIG_FILE);
			JAXBContext context = JAXBContext.newInstance(XMLRoot.class);
			Unmarshaller um = context.createUnmarshaller();
			XMLRoot xmlroot = (XMLRoot) um.unmarshal(in);
			for(XMLNode xmlnode : xmlroot.nodes)
			{
				Object value = null;
				switch(xmlnode.type)
				{
					case BOOLEAN:
						value = Boolean.parseBoolean(xmlnode.value);
						break;
					case NUMBER:
						value = Integer.parseInt(xmlnode.value);
						break;
					case STRING:
						value = xmlnode.value;
						break;
					case FONT:
						value = new Font(xmlnode.value, Font.PLAIN, 12);
						break;
					case COLOR:
						String[] values = xmlnode.value.split(",");
						value = new Color(Integer.parseInt(values[0]),
							Integer.parseInt(values[1]),
							Integer.parseInt(values[2]),
							Integer.parseInt(values[3]));
						break;
				}
				if(value == null)
				{
					Logger.logWarning(TAG + "could not unserialize configuration key '" + xmlnode.key + "': unexpected type of data '" + xmlnode.type + "'.");
				} else {
					values.put(xmlnode.key, value);
				}
			}
		} catch (NullPointerException npe) {
			try { in.close(); } catch (Exception e) { }
			throw new ConfigurationException(npe);
		} catch (JAXBException jaxbe) {
			try { in.close(); } catch (Exception e) { }
			throw new ConfigurationException(jaxbe);
		} catch (FileNotFoundException fnfe) {
			throw new ConfigurationException(fnfe);
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
		try { in.close(); } catch (Exception e) { }
	}

	@Override
	public synchronized void configSave() throws ConfigurationException
	{
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(Configuration.CONFIG_FILE);
			JAXBContext context = JAXBContext.newInstance(XMLRoot.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			XMLRoot xmlroot = new XMLRoot();
			{
				for(String key : values.keySet())
				{
					Object value = values.get(key);
					XMLNode xmlnode = new XMLNode();
					xmlnode.key = key;
					xmlnode.value = null;
					if(value instanceof Boolean)
					{
						xmlnode.value = ((Boolean) value).toString();
						xmlnode.type = Type.BOOLEAN;
					}
					if(value instanceof Integer)
					{
						xmlnode.value = ((Integer) value).toString();
						xmlnode.type = Type.NUMBER;
					}
					if(value instanceof String)
					{
						xmlnode.value = ((String) value);
						xmlnode.type = Type.STRING;
					}
					if(value instanceof Font)
					{
						xmlnode.value = ((Font) value).getName();
						xmlnode.type = Type.FONT;
					}
					if(value instanceof Color)
					{
						Color color = (Color) value;
						xmlnode.value = "" + color.getRed() + "," + 
											color.getGreen() + "," + 
											color.getBlue() + "," +
											color.getAlpha();
						xmlnode.type = Type.COLOR;
					}
					if(xmlnode.value == null)
					{
						Logger.logWarning(TAG + "could not serialize configuration key '" + key + "': unexpected type of data '" + value.getClass().getCanonicalName() + "'.");
					} else {
						xmlroot.nodes.add(xmlnode);
					}
				}
			}
			m.marshal(xmlroot, out);
		} catch (NullPointerException npe) {
			try { out.close(); } catch (Exception e) { }
			throw new ConfigurationException(npe);
		} catch (JAXBException jaxbe) {
			try { out.close(); } catch (Exception e) { }
			throw new ConfigurationException(jaxbe);
		} catch (FileNotFoundException fnfe) {
			throw new ConfigurationException(fnfe);
		}
		try { out.close(); } catch (Exception e) { }
	}
	
	@XmlRootElement(namespace = "org.dyndns.doujindb.conf", name="Root")
	private static final class XMLRoot
	{
		@XmlElements({
		    @XmlElement(name="Node", type=XMLNode.class)
		  })
		private List<XMLNode> nodes = new Vector<XMLNode>();
	}
	
	@XmlRootElement(namespace = "org.dyndns.doujindb.conf", name="Node")
	private static final class XMLNode
	{
		@XmlAttribute(name="Key", required=true)
		private String key;
		@XmlAttribute(name="Type", required=true)
		private Type type;
		@XmlAttribute(name="Value")
		private String value;
	}
}
