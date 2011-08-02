package org.dyndns.doujindb.db;

/**  
* DatabaseException.java
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class DatabaseException extends RuntimeException
{

	public DatabaseException() { }

	public DatabaseException(String message) { super(message); }

	public DatabaseException(Throwable cause) { super(cause); }

	public DatabaseException(String message, Throwable cause) { super(message, cause); }

}
