package org.dyndns.doujindb.log.impl;

import org.dyndns.doujindb.log.*;

/**  
* ImplEvent.java - A log event.
* @author  nozomu
* @version 1.0
*/
final class ImplEvent implements LogEvent
{	
	private String message;
	private Object source;
	private long timestamp;
	private Level level;
	
	ImplEvent(String message, Level level)
	{
		this.message = message;
		this.level = level;
		/* _BEGIN_HACK_ */
		String source = new Throwable().fillInStackTrace().getStackTrace()[2].getClassName();
		source = source.substring(source.lastIndexOf('.') != -1 ? source.lastIndexOf('.') + 1 : 0);
		/* _END_HACK_ */
		this.source = source;
		this.timestamp = System.currentTimeMillis();
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
