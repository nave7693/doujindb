package org.dyndns.doujindb.db;

@SuppressWarnings("serial")
public class DataBaseException extends RuntimeException
{
	public DataBaseException() { }
	public DataBaseException(String message) { super(message); }
	public DataBaseException(Throwable cause) { super(cause); }
	public DataBaseException(String message, Throwable cause) { super(message, cause); }
}