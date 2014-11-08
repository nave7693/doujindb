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
		BOOLEAN (java.lang.Boolean.class),
		INTEGER (java.lang.Integer.class),
		FLOAT (java.lang.Float.class),
		STRING (java.lang.String.class),
		FONT (java.awt.Font.class),
		COLOR (java.awt.Color.class),
		FILE (java.io.File.class),
		LOG (java.util.logging.Level.class);
		
		private Class<?> clazz;
		
		Type(Class<?> clazz) {
			this.clazz = clazz;
		}
		
		public final Class<?> getBaseClass() {
			return clazz;
		}
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
