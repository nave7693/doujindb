package org.dyndns.doujindb.log.impl;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.dyndns.doujindb.log.*;

/**  
* SystemLogger.java - Logger writing on the standard output.
* @author  nozomu
* @version 1.0
*/
final class SystemLogger implements Logger
{
	private Vector<Logger> loggers = new Vector<Logger>();
	private OutputStream stream = System.out;
	private SimpleDateFormat sdf;
	
	SystemLogger()
	{
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	@Override
	public void log(LogEvent event)
	{
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
							event.getMessage()
						).getBytes());
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
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