package org.dyndns.doujindb.core.log;

import java.lang.management.ManagementFactory;

import org.dyndns.doujindb.log.*;

/**  
* ImplEvent.java - A log event.
* @author  nozomu
* @version 1.0
*/
final class ImplEvent implements Event
{	
	private String message;
	private Object source;
	private long timestamp;
	private Level level;
	
	ImplEvent(String message, Level level)
	{
		this.message = message;
		this.level = level;
		this.source = new Throwable().fillInStackTrace().getStackTrace()[2].getClassName();
		this.timestamp = ManagementFactory.getRuntimeMXBean().getUptime();
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public Level getLevel()
	{
		return level;
	}
	
	public Object getSource()
	{
		return source;
	}
	
	public long getTime()
	{
		return timestamp;
	}
}
