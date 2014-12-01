package org.dyndns.doujindb.plug.impl.dataimport;

@SuppressWarnings("serial")
public final class TaskErrorException extends RuntimeException
{
	public TaskErrorException() { }

	public TaskErrorException(String message) { super(message); }
	
	public TaskErrorException(Throwable cause) { super(cause); }
	
	public TaskErrorException(String message, Throwable cause) { super(message, cause); }
}
