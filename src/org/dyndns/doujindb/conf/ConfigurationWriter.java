package org.dyndns.doujindb.conf;

import java.io.*;
import java.lang.reflect.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public final class ConfigurationWriter
{
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(ConfigurationWriter.class);
	
	private static void parseConfiguration(Class<?> config)
	{
		LOG.debug("call parseConfiguration({})", config);
		for(Field field : config.getDeclaredFields())
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
}
