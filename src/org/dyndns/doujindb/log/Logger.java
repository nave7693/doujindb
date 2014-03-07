package org.dyndns.doujindb.log;

/**  
* Logger.java - Logging interface.
* @author  nozomu
* @version 1.1
*/
public interface Logger
{
	public abstract void log(LogEvent log);
	public abstract void logFatal(String message);
	public abstract void logFatal(String message, Throwable err);
	public abstract void logDebug(String message);
	public abstract void logDebug(String message, Throwable err);
	public abstract void logError(String message);
	public abstract void logError(String message, Throwable err);
	public abstract void logWarning(String message);
	public abstract void logWarning(String message, Throwable err);
	public abstract void logInfo(String message);
	public abstract void logInfo(String message, Throwable err);
	public abstract void loggerAttach(Logger logger);
	public abstract void loggerDetach(Logger logger);
	
	public final class LogEvent
	{	
		private String message;
		private long timestamp;
		private Level level;
		private Throwable throwable;

		public LogEvent(Level level, String message, Throwable err)
		{
			this.message = message;
			this.level = level;
			this.timestamp = System.currentTimeMillis();
			this.throwable = err;
		}
		
		public LogEvent(Level level, String message)
		{
			this(level, message, null);
		}

		public String getMessage()
		{
			return message;
		}
		
		public Level getLevel()
		{
			return level;
		}

		public long getTime()
		{
			return timestamp;
		}
		
		public Throwable getThrowable()
		{
			return throwable;
		}
	}
}
