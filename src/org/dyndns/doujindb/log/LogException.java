package org.dyndns.doujindb.log;

/**  
* LogException.java
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class LogException extends RuntimeException
{

	public LogException() { }

	public LogException(String message) { super(message); }

	public LogException(Throwable cause) { super(cause); }

	public LogException(String message, Throwable cause) { super(message, cause); }

}