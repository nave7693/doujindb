package org.dyndns.doujindb.log.impl;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingWorker;

import org.dyndns.doujindb.log.*;

/**  
* SystemLogger.java - Logger writing on the standard output.
* @author  nozomu
* @version 1.0
*/
public final class SystemLogger implements Logger
{
	private List<Logger> loggers = new Vector<Logger>();
	private ConcurrentLinkedQueue<LogEvent> queue = new ConcurrentLinkedQueue<LogEvent>();

	private SimpleDateFormat sdf;
	
	private PrintWriter stdout;
	
	public SystemLogger()
	{
		/**
		 * ISO 8601 Data elements and interchange formats – Information interchange – Representation of dates and times
		 * @see http://en.wikipedia.org/wiki/ISO_8601
		 */
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		stdout = new PrintWriter(new OutputStreamWriter(System.out));

		new SwingWorker<Void, LogEvent>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				Thread.currentThread().setName("system-logger");
				while(true)
				{
					try
					{
						Thread.sleep(1);
						if(queue.isEmpty())
							continue;
						publish(queue.poll());
					} catch (Exception e) {
						e.printStackTrace();
					} catch (Error e) {
						e.printStackTrace();
						break;
					}
				}
				return null;
			}
			
			@Override
			protected void process(List<LogEvent> events)
			{
				for(LogEvent log : events)
				{
					stdout.printf("%s [%s] %s\r\n", sdf.format(new Date(log.getTime())),
						log.getLevel(),
						log.getMessage());
					if(log.getThrowable() != null)
						log.getThrowable().printStackTrace(stdout);
				}
				stdout.flush();
			}
		}.execute();
	}
	
	@Override
	public synchronized void log(LogEvent log)
	{
		queue.offer(log);
		for(Logger logger : loggers)
			logger.log(log);
	}

	@Override
	public synchronized void loggerAttach(Logger logger)
	{
		if(!loggers.contains(logger))
			loggers.add(logger);
	}

	@Override
	public synchronized void loggerDetach(Logger logger)
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
}
