package org.dyndns.doujindb.log;

import java.lang.management.ManagementFactory;

/**  
* LogEvent.java - Logging event.
* @author  nozomu
* @version 1.0
*/
public final class LogEvent
{	
	private String message;
	private Object source;
	private long time;
	private LogLevel level;
	
	public LogEvent(String message, LogLevel level)
	{
		this.message = message;
		this.level = level;
		this.source = new Throwable().fillInStackTrace().getStackTrace()[1].getClassName();
		this.time = ManagementFactory.getRuntimeMXBean().getUptime();
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public LogLevel getLevel()
	{
		return level;
	}
	
	public Object getSource()
	{
		return source;
	}
	
	public long getTime()
	{
		return time;
	}
}
