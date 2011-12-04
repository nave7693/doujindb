package org.dyndns.doujindb.log;

/**  
* Event.java - Logging event.
* @author  nozomu
* @version 1.0
*/
public interface Event
{
	public String getMessage();
	
	public Level getLevel();
	
	public Object getSource();
	
	public long getTime();
}