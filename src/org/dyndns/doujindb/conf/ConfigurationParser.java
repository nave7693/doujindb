package org.dyndns.doujindb.conf;

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public final class ConfigurationParser
{
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(ConfigurationParser.class);
	
	@SuppressWarnings("rawtypes")
	private static final HashMap<Class<?>, ItemParser> itemParsers = new HashMap<Class<?>, ItemParser>();
	
	static
	{
		itemParsers.put(String.class, new ItemParser<String>() {
			@Override
			public String fromString(String data) throws IllegalArgumentException {
				return data;
			}
			@Override
			public String toString(String item) throws IllegalArgumentException {
				return item;
			}
		});
		itemParsers.put(Integer.class, new ItemParser<Integer>() {
			@Override
			public Integer fromString(String data) throws IllegalArgumentException {
				try {
					return Integer.parseInt(data);
				} catch (NumberFormatException nfe) {
					throw new IllegalArgumentException(nfe);
				}
			}
			@Override
			public String toString(Integer item) throws IllegalArgumentException {
				return item.toString();
			}
		});
		itemParsers.put(Float.class, new ItemParser<Float>() {
			@Override
			public Float fromString(String data) throws IllegalArgumentException {
				try {
					return Float.parseFloat(data);
				} catch (NumberFormatException nfe) {
					throw new IllegalArgumentException(nfe);
				}
			}
			@Override
			public String toString(Float item) throws IllegalArgumentException {
				return item.toString();
			}
		});
		itemParsers.put(Boolean.class, new ItemParser<Boolean>() {
			@Override
			public Boolean fromString(String data) throws IllegalArgumentException {
				return Boolean.parseBoolean(data);
			}
			@Override
			public String toString(Boolean item) throws IllegalArgumentException {
				return item.toString();
			}
		});
		itemParsers.put(Level.class, new ItemParser<Level>() {
			@Override
			public Level fromString(String data) throws IllegalArgumentException {
				return Level.valueOf(data);
			}
			@Override
			public String toString(Level item) throws IllegalArgumentException {
				return item.toString();
			}
		});
		itemParsers.put(Color.class, new ItemParser<Color>() {
			@Override
			public Color fromString(String data) throws IllegalArgumentException {
				try {
					String[] argb = data.split(":");
					return new Color(Integer.parseInt(argb[0]), Integer.parseInt(argb[1]), Integer.parseInt(argb[2]), Integer.parseInt(argb[3]));
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			}
			@Override
			public String toString(Color item) throws IllegalArgumentException {
				return String.format("%d:%d:%d:%d", item.getAlpha(), item.getRed(), item.getGreen(), item.getBlue());
			}
		});
		itemParsers.put(File.class, new ItemParser<File>() {
			@Override
			public File fromString(String data) throws IllegalArgumentException {
				try {
					return new File(data);
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			}
			@Override
			public String toString(File item) throws IllegalArgumentException {
				try {
					return item.getCanonicalPath();
				} catch (IOException ioe) {
					throw new IllegalArgumentException(ioe);
				}
			}
		});
		itemParsers.put(Font.class, new ItemParser<Font>() {
			@Override
			public Font fromString(String data) throws IllegalArgumentException {
				try {
					String[] fontData = data.split(":");
					return new Font(fontData[0], Integer.parseInt(fontData[1]), Integer.parseInt(fontData[2]));
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			}
			@Override
			public String toString(Font item) throws IllegalArgumentException {
				return String.format("%s:%d:%d", item.getFontName(), item.getSize(), item.getStyle());
			}
		});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static XMLConfiguration parseConfiguration(Class<?> config)
	{
		LOG.debug("call parseConfiguration({})", config);
		XMLConfiguration xmlConfig = new XMLConfiguration();
		for(Field field : config.getDeclaredFields())
		{
			if(field.getType().equals(ConfigurationItem.class))
			{
				String configName = field.getName().replaceAll("_", ".");
				try {
					ConfigurationItem<?> configItem = (ConfigurationItem<?>) field.get(config);
					XMLConfigurationItem xmlConfigItem = new XMLConfigurationItem();
					xmlConfigItem.key = configName;
					xmlConfigItem.type = configItem.getType().getName();
					ItemParser parser = itemParsers.get(configItem.getType());
					if(parser == null) {
						LOG.warn("Error parsing ConfigurationItem [{}]: unsupported type [{}]", xmlConfigItem.key, xmlConfigItem.type);
						continue;
					} else {
						xmlConfigItem.value = parser.toString(configItem.get());
					}
					xmlConfig.items.add(xmlConfigItem);
					LOG.debug("Parsed ConfigurationItem [{}, {}]", xmlConfigItem.key, xmlConfigItem.value);
				} catch (IllegalArgumentException | IllegalAccessException iae) {
					LOG.debug("Error parsing ConfigurationItem [{}]", configName, iae);
				}
			}
		}
		return xmlConfig;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static XMLConfiguration parseConfiguration(Object config)
	{
		LOG.debug("call parseConfiguration({})", config);
		XMLConfiguration xmlConfig = new XMLConfiguration();
		for(Field field : config.getClass().getDeclaredFields())
		{
			if(field.getType().equals(ConfigurationItem.class))
			{
				String configName = field.getName().replaceAll("_", ".");
				try {
					ConfigurationItem<?> configItem = (ConfigurationItem<?>) field.get(config);
					XMLConfigurationItem xmlConfigItem = new XMLConfigurationItem();
					xmlConfigItem.key = configName;
					xmlConfigItem.type = configItem.getType().getName();
					ItemParser parser = itemParsers.get(configItem.getType());
					if(parser == null) {
						LOG.warn("Error parsing ConfigurationItem [{}]: unsupported type [{}]", xmlConfigItem.key, xmlConfigItem.type);
						continue;
					} else {
						xmlConfigItem.value = parser.toString(configItem.get());
					}
					xmlConfig.items.add(xmlConfigItem);
					LOG.debug("Parsed ConfigurationItem [{}, {}]", xmlConfigItem.key, xmlConfigItem.value);
				} catch (IllegalArgumentException | IllegalAccessException iae) {
					LOG.debug("Error parsing ConfigurationItem [{}]", configName, iae);
				}
			}
		}
		return xmlConfig;
	}
	
	public static void toXML(Class<?> config, OutputStream out) throws IOException
	{
		LOG.debug("call toXML({}, {})", config, out);
		XMLConfiguration xmlConfig = parseConfiguration(config);
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLConfiguration.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(xmlConfig, out);
		} catch (NullPointerException npe) {
			throw new ConfigurationException(npe);
		} catch (JAXBException jaxbe) {
			throw new ConfigurationException(jaxbe);
		}
	}
	
	public static void toXML(Class<?> config, File file) throws IOException
	{
		LOG.debug("call toXML({}, {})", config, file);
		FileOutputStream out = new FileOutputStream(file);
		toXML(config, out);
		out.close();
	}
	
	public static void toXML(Object config, OutputStream out) throws IOException
	{
		LOG.debug("call toXML({}, {})", config, out);
		XMLConfiguration xmlConfig = parseConfiguration(config);
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLConfiguration.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(xmlConfig, out);
		} catch (NullPointerException npe) {
			throw new ConfigurationException(npe);
		} catch (JAXBException jaxbe) {
			throw new ConfigurationException(jaxbe);
		}
	}
	
	public static void toXML(Object config, File file) throws IOException
	{
		LOG.debug("call toXML({}, {})", config, file);
		FileOutputStream out = new FileOutputStream(file);
		toXML(config, out);
		out.close();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void fromXML(Class<?> config, InputStream in) throws IOException
	{
		LOG.debug("call fromXML({}, {})", config, in);
		//TODO
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLConfiguration.class);
			Unmarshaller um = context.createUnmarshaller();
			XMLConfiguration xmlConfig = (XMLConfiguration) um.unmarshal(in);
			// create a key=>value map which is simpler and faster to access
			HashMap<String, XMLConfigurationItem> items = new HashMap<String, XMLConfigurationItem>();
			for(XMLConfigurationItem item : xmlConfig.items)
				items.put(item.key, item);
			// try loading every field that is a ConfigurationItem
			for(Field field : config.getDeclaredFields())
			{
				if(field.getType().equals(ConfigurationItem.class))
				{
					String configName = field.getName().replaceAll("_", ".");
					if(!items.containsKey(configName))
						continue;
					try {
						ConfigurationItem configItem = (ConfigurationItem) field.get(config);
						ItemParser parser = itemParsers.get(configItem.getType());
						if(parser == null) {
							continue;
						}
						configItem.set(parser.fromString(items.get(configName).value));
					} catch (IllegalArgumentException | IllegalAccessException iae) {
						LOG.debug("Error parsing ConfigurationItem [{}]", configName, iae);
					}
				}
			}
		} catch (NullPointerException npe) {
			throw new ConfigurationException(npe);
		} catch (JAXBException jaxbe) {
			throw new ConfigurationException(jaxbe);
		}
	}
	
	public static void fromXML(Class<?> config, File file) throws IOException
	{
		LOG.debug("call fromXML({}, {})", config, file);
		FileInputStream in = new FileInputStream(file);
		fromXML(config, in);
		in.close();
	}
	
	public static void fromXML(Object config, InputStream in) throws IOException
	{
		LOG.debug("call fromXML({}, {})", config, in);
		//TODO
		try
		{
			JAXBContext context = JAXBContext.newInstance(XMLConfiguration.class);
			Unmarshaller um = context.createUnmarshaller();
			XMLConfiguration xmlConfig = (XMLConfiguration) um.unmarshal(in);
		} catch (NullPointerException npe) {
			throw new ConfigurationException(npe);
		} catch (JAXBException jaxbe) {
			throw new ConfigurationException(jaxbe);
		}
	}
	
	public static void fromXML(Object config, File file) throws IOException
	{
		LOG.debug("call fromXML({}, {})", config, file);
		FileInputStream in = new FileInputStream(file);
		fromXML(config, in);
		in.close();
	}
	
	public static void toJSON(Object config, OutputStream out) throws IOException
	{
		LOG.debug("call toJSON({}, {})", config, out);
		//TODO parseConfiguration(config);
		throw new IOException("Method toJSON() not yet implemented");
	}
	
	public static void toJSON(Object config, File file) throws IOException
	{
		LOG.debug("call toJSON({}, {})", config, file);
		FileOutputStream out = new FileOutputStream(file);
		toJSON(config, out);
		out.close();
	}
	
	@XmlRootElement(namespace="org.dyndns.doujindb.conf", name="Configuration")
	private static final class XMLConfiguration
	{
		@XmlElements({
			@XmlElement(name="ConfigurationItem", type=XMLConfigurationItem.class)
		})
		private List<XMLConfigurationItem> items = new Vector<XMLConfigurationItem>();
	}
	
	@XmlRootElement(namespace="org.dyndns.doujindb.conf", name="ConfigurationItem")
	private static final class XMLConfigurationItem
	{
		@XmlAttribute(name="key", required=true)
		private String key;
		@XmlAttribute(name="type", required=true)
		private String type;
		@XmlAttribute(name="value")
		private String value;
	}
	
	interface ItemParser<T>
	{
		public abstract T fromString(String data) throws IllegalArgumentException;
		public abstract String toString(T item) throws IllegalArgumentException;
	}
}
