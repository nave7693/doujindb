package org.dyndns.doujindb.plug.impl.dataimport;

@SuppressWarnings("serial")
public final class TaskException extends RuntimeException
{
	public TaskException() { }

	public TaskException(String message) { super(message); }
	
	public TaskException(Throwable cause) { super(cause); }
	
	public TaskException(String message, Throwable cause) { super(message, cause); }
}
