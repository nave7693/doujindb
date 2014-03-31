package org.dyndns.doujindb.log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**  
* SystemLogger.java - Logger writing on the standard output.
* @author  nozomu
* @version 1.2
*/
final class SystemLogger implements ILogger
{
	private List<ILogger> loggers = new Vector<ILogger>();
	private ConcurrentLinkedQueue<LogEvent> queue = new ConcurrentLinkedQueue<LogEvent>();

	private SimpleDateFormat sdf;
	
	private PrintWriter stdout;
	
	private Boolean logDebug = false;
	
	public SystemLogger()
	{
		/**
		 * ISO 8601 Data elements and interchange formats – Information interchange – Representation of dates and times
		 * @see http://en.wikipedia.org/wiki/ISO_8601
		 */
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		stdout = new PrintWriter(new OutputStreamWriter(System.out));

		new Thread()
		{
			@Override
			public void run()
			{
				Thread.currentThread().setName("system-logger");
				while(true)
				{
					try
					{
						Thread.sleep(1);
						if(queue.isEmpty())
							continue;
						processLog(queue.poll());
					} catch (Exception e) {
						e.printStackTrace();
					} catch (Error e) {
						e.printStackTrace();
						break;
					}
				}
			}
			
			private void processLog(LogEvent event)
			{
				if(event.getLevel().equals(Level.DEBUG) && !logDebug)
					return;
				stdout.printf("%s [%s] %s\r\n", sdf.format(new Date(event.getTime())),
						event.getLevel(),
						event.getMessage());
					if(event.getThrowable() != null)
						event.getThrowable().printStackTrace(stdout);
				stdout.flush();
			}
		}.start();
	}
	
	@Override
	public synchronized void log(LogEvent event)
	{
		queue.offer(event);
		for(ILogger logger : loggers)
			logger.log(event);
	}

	@Override
	public synchronized void loggerAttach(ILogger logger)
	{
		if(!loggers.contains(logger))
			loggers.add(logger);
	}

	@Override
	public synchronized void loggerDetach(ILogger logger)
	{
		loggers.remove(logger);
	}

	@Override
	public void logFatal(String message) {
		log(new LogEvent(Level.FATAL, message));
	}

	@Override
	public void logFatal(String message, Throwable err) {
		log(new LogEvent(Level.FATAL, message, err));
	}

	@Override
	public void logDebug(String message) {
		log(new LogEvent(Level.DEBUG, message));
	}

	@Override
	public void logDebug(String message, Throwable err) {
		log(new LogEvent(Level.DEBUG, message, err));
	}

	@Override
	public void logError(String message) {
		log(new LogEvent(Level.ERROR, message));
	}

	@Override
	public void logError(String message, Throwable err) {
		log(new LogEvent(Level.ERROR, message, err));
	}

	@Override
	public void logWarning(String message) {
		log(new LogEvent(Level.WARNING, message));
	}

	@Override
	public void logWarning(String message, Throwable err) {
		log(new LogEvent(Level.WARNING, message, err));
	}

	@Override
	public void logInfo(String message) {
		log(new LogEvent(Level.INFO, message));
	}

	@Override
	public void logInfo(String message, Throwable err) {
		log(new LogEvent(Level.INFO, message, err));
	}

	@Override
	public void enableDebug(boolean logDebug) {
		this.logDebug = logDebug;
	}
}
