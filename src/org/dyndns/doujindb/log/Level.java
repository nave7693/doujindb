package org.dyndns.doujindb.log;

/**  
* Level.java - Defines the basic 5 types of logging event
* @author  nozomu
* @version 1.0
*/
public enum Level
{
	INFO,
	DEBUG, // Should not be used in production environment
	WARNING,
	ERROR,
	FATAL
}
