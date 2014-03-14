package org.dyndns.doujindb.conf;

/**  
* IConfiguration.java - Configuration interface
* @author  nozomu
* @version 1.0
*/
interface IConfiguration
{
	public static enum Type
	{
		BOOLEAN,
		NUMBER,
		STRING,
		FONT,
		COLOR,
	}
	
	public Object configRead(String key) throws ConfigurationException;

	public void configWrite(String key, Object value) throws ConfigurationException;

	public void configAdd(String key, String info, Object value) throws ConfigurationException;

	public void configRemove(String key) throws ConfigurationException;
	
	public boolean configExists(String key);

	public String configInfo(String key) throws ConfigurationException;

	public Iterable<String> keys();
	
	public Iterable<Object> values();
	
	public void configLoad() throws ConfigurationException;
	
	public void configSave() throws ConfigurationException;
}
