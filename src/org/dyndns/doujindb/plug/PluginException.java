package org.dyndns.doujindb.plug;

/**  
* PluginException.java - Exception
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class PluginException extends Exception
{
	public PluginException() { }

	public PluginException(String message) { super(message); }
	
	public PluginException(Throwable cause) { super(cause); }
	
	public PluginException(String message, Throwable cause) { super(message, cause); }
}
