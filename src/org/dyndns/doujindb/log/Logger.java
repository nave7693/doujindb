package org.dyndns.doujindb.log;

/**  
* Logger.java - Logging interface.
* @author  nozomu
* @version 1.0
*/
public interface Logger
{
	public void log(LogEvent event);
	public void loggerAttach(Logger logger);
	public void loggerDetach(Logger logger);
}
