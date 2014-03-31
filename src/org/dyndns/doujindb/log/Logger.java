package org.dyndns.doujindb.log;

import org.dyndns.doujindb.log.ILogger.LogEvent;

/**  
* Logger.java - Logging Gateway.
* @author  nozomu
* @version 1.2
*/
public final class Logger
{
	private static ILogger instance = new SystemLogger();
	
	public static synchronized void log(LogEvent event)
	{
		instance.log(event);
	}

	public static void logFatal(String message) {
		log(new LogEvent(Level.FATAL, message));
	}

	public static void logFatal(String message, Throwable err) {
		log(new LogEvent(Level.FATAL, message, err));
	}

	public static void logDebug(String message) {
		log(new LogEvent(Level.DEBUG, message));
	}

	public static void logDebug(String message, Throwable err) {
		log(new LogEvent(Level.DEBUG, message, err));
	}

	public static void logError(String message) {
		log(new LogEvent(Level.ERROR, message));
	}

	public static void logError(String message, Throwable err) {
		log(new LogEvent(Level.ERROR, message, err));
	}

	public static void logWarning(String message) {
		log(new LogEvent(Level.WARNING, message));
	}

	public static void logWarning(String message, Throwable err) {
		log(new LogEvent(Level.WARNING, message, err));
	}

	public static void logInfo(String message) {
		log(new LogEvent(Level.INFO, message));
	}

	public static void logInfo(String message, Throwable err) {
		log(new LogEvent(Level.INFO, message, err));
	}
	
	public static void enableDebug(boolean logDebug) {
		instance.enableDebug(logDebug);
	}
}
