package org.dyndns.doujindb.net;

/**  
* NetworkException.java
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class NetworkException extends RuntimeException
{

	public NetworkException() { }

	public NetworkException(String message) { super(message); }

	public NetworkException(Throwable cause) { super(cause); }

	public NetworkException(String message, Throwable cause) { super(message, cause); }

}
