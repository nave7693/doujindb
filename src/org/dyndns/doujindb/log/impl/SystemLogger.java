package org.dyndns.doujindb.log.impl;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.log.*;

/**  
* SystemLogger.java - Logger writing on the standard output.
* @author  nozomu
* @version 1.0
*/
final class SystemLogger implements Logger
{
	private List<Logger> loggers = new Vector<Logger>();
	private OutputStream stream = System.out;
	private LinkedList<LogEvent> buffer = new LinkedList<LogEvent>();
    private final int MAX_LOG_BUFFER = 0xFF;

	private SimpleDateFormat sdf;
	
	SystemLogger()
	{
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		new Thread(getClass().getName()+"$EventPoller")
		{
			@Override
			public void run()
			{
				super.setPriority(Thread.MIN_PRIORITY);
				while(true)
				{
					if(!buffer.isEmpty())
					{
						LogEvent event = buffer.peek();
						try
						{
							String level_string = "";
							switch(event.getLevel())
							{
							case INFO:
								level_string = "Info";
								break;
							case WARNING:
								level_string = "Warning";
								break;
							case ERROR:
								level_string = "Error";
								break;
							case DEBUG:
								level_string = "Debug";
								break;
							case FATAL:
								level_string = "Fatal";
								break;
							}
							stream.write(
									String.format(sdf.format(new Date(event.getTime())) + " [%s] %s: %s\r\n",
											level_string,
											event.getSource(),
											event.getMessage()).getBytes()
										);
							buffer.poll();
						} catch (IOException ioe) { ioe.printStackTrace(); }
					} else
						try { sleep(100); } catch (InterruptedException ie) { }
				}
			}
		}.start();
	}
	
	@Override
	public void log(LogEvent event)
	{
		if(buffer.size() > MAX_LOG_BUFFER)
			Core.Logger.log("File logger exceeded max number of cached entries.", Level.ERROR);
		else
			buffer.offer(event);
		for(Logger logger : loggers)
			logger.log(event);
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
	public void log(String message, Level level)
	{
		log(new ImplEvent(message, level));
	}
}