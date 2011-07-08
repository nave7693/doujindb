package org.dyndns.doujindb.dat;

/**  
* DataStoreException.java
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class DataStoreException extends RuntimeException
{

	public DataStoreException() { }

	public DataStoreException(String message) { super(message); }

	public DataStoreException(Throwable cause) { super(cause); }

	public DataStoreException(String message, Throwable cause) { super(message, cause); }

}