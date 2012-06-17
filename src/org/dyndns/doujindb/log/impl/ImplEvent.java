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
		/* _END_HACK_ */
		this.source = source;
		this.timestamp = System.currentTimeMillis();
	}

	@Override
	public String getMessage()
	{
		return message;
	}
	
	@Override
	public Level getLevel()
	{
		return level;
	}
	
	@Override
	public Object getSource()
	{
		return source;
	}
	
	@Override
	public long getTime()
	{
		return timestamp;
	}
}
