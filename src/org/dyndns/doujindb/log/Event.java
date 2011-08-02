package org.dyndns.doujindb.log;

//TODO import java.lang.management.ManagementFactory;

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