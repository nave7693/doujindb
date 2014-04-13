package org.dyndns.doujindb.db;

/**  
* DataBaseException.java
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class DataBaseException extends RuntimeException
{
	public DataBaseException() { }
	public DataBaseException(String message) { super(message); }
	public DataBaseException(Throwable cause) { super(cause); }
	public DataBaseException(String message, Throwable cause) { super(message, cause); }
}