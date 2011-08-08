package org.dyndns.doujindb.log.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.log.*;

import static org.dyndns.doujindb.Core.Logger;;

/**  
* FileLogger.java - Logger writing on a local file.
* @author  nozomu
* @version 1.0
*/
final class FileLogger implements Logger
{
	private Vector<Logger> loggers = new Vector<Logger>();
	private RandomAccessFile writer;
	private LinkedList<Event> buffer = new LinkedList<Event>();
	private final int MAX_LOG_BUFFER = 0x80;
	
	FileLogger(File out) throws FileNotFoundException
	{
		writer = new RandomAccessFile(out, "rw");
		try
		{
			writer.seek(writer.length());
			writer.writeBytes("----------------------------\r\n");
			writer.writeBytes(" DoujinDB session started.\r\n");
			writer.writeBytes(" " + new Date() + "\r\n");
			writer.writeBytes("----------------------------\r\n");
		} catch (IOException ioe) { }
		new Thread()
		{
			@Override
			public void run()
			{
				super.setPriority(Thread.MIN_PRIORITY);
				while(true)
				{
					if(!buffer.isEmpty())
					{
						Event event = buffer.peek();
						String level_string = "";
						switch(event.getLevel())
						{
							case INFO:
								level_string = "    ";
								break;
							case WARNING:
								level_string = "<W> ";
								break;
							case ERROR:
								level_string = "<E> ";
								break;
						}
						try
						{
							writer.writeBytes(
									String.format("%s%s\r\n",
											level_string,
											event.getMessage()
										));
							buffer.poll();
							writer.setLength(writer.length());
						} catch (IOException ioe) {
							Logger.log(ioe.getMessage(), Level.ERROR);
						}
					} else
						try { sleep(100); } catch (InterruptedException e) { }
				}
			}
		}.start();
	}
	
	@Override
	public void log(Event event)
	{
		if(buffer.size() > MAX_LOG_BUFFER)
			Logger.log("File logger exceeded max number of cached entries.", Level.ERROR);
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
