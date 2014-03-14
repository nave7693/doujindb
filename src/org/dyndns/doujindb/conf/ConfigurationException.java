package org.dyndns.doujindb.conf;

/**  
* ConfigurationException.java
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class ConfigurationException extends RuntimeException
{

	public ConfigurationException() { }

	public ConfigurationException(String message) { super(message); }

	public ConfigurationException(Throwable cause) { super(cause); }

	public ConfigurationException(String message, Throwable cause) { super(message, cause); }

}
