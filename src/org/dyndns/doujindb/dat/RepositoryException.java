package org.dyndns.doujindb.dat;

import java.io.IOException;

/**  
* RepositoryException.java
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class RepositoryException extends IOException
{

	public RepositoryException() { }

	public RepositoryException(String message) { super(message); }

	public RepositoryException(Throwable cause) { super(cause); }

	public RepositoryException(String message, Throwable cause) { super(message, cause); }

}