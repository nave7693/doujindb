package org.dyndns.doujindb.plug.impl.dataimport;

@SuppressWarnings("serial")
public final class TaskWarningException extends RuntimeException
{
	public TaskWarningException() { }

	public TaskWarningException(String message) { super(message); }
	
	public TaskWarningException(Throwable cause) { super(cause); }
	
	public TaskWarningException(String message, Throwable cause) { super(message, cause); }
}
