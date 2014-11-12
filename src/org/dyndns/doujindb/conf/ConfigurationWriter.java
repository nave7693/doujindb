package org.dyndns.doujindb.conf;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public final class ConfigurationWriter
{
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(ConfigurationWriter.class);
	
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
					XMLConfigurationItem xmlConfigItem = new XMLConfigurationItem();
					xmlConfigItem.key = configName;
					xmlConfigItem.value = field.get(config).toString(); //FIXME
					xmlConfigItem.type = field.get(config).getClass().toString(); //FIXME
					xmlConfig.items.add(xmlConfigItem);
					LOG.debug("Parsed ConfigurationItem [{}, {}]", xmlConfigItem.key, xmlConfigItem.value);
				} catch (IllegalArgumentException | IllegalAccessException iae) {
					LOG.debug("Error parsing ConfigurationItem [{}]", configName, iae);
				}
			}
		}
		return xmlConfig;
	}
	
	private static void parseConfiguration(Object config)
	{
		LOG.debug("call parseConfiguration({})", config);
		for(Field field : config.getClass().getDeclaredFields())
		{
			if(field.getType().equals(ConfigurationItem.class))
			{
				String configName = field.getName().replaceAll("_", ".");
				try {
					LOG.debug("Parsed ConfigurationItem [{}, {}]", configName, field.get(config));
				} catch (IllegalArgumentException | IllegalAccessException iae) {
					LOG.debug("Error parsing ConfigurationItem [{}]", configName, iae);
				}
			}
		}
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
		} finally {
			try { out.close(); } catch (Exception e) { }
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
		//TODO parseConfiguration(config);
	}
	
	public static void toXML(Object config, File file) throws IOException
	{
		LOG.debug("call toXML({}, {})", config, file);
		FileOutputStream out = new FileOutputStream(file);
		toXML(config, out);
		out.close();
	}
	
	public static void toJSON(Object config, OutputStream out) throws IOException
	{
		LOG.debug("call toJSON({}, {})", config, out);
		//TODO parseConfiguration(config);
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
}
