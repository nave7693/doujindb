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
final class SystemLogger implements Logger
{
	private List<Logger> loggers = new Vector<Logger>();
	private ConcurrentLinkedQueue<LogEvent> queue = new ConcurrentLinkedQueue<LogEvent>();

	private SimpleDateFormat sdf;
	
	SystemLogger()
	{
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		new SwingWorker<Void, LogEvent>()
		{
			@Override
			protected Void doInBackground() throws Exception {
				Thread.currentThread().setName("SystemLogger");
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
			protected void process(List<LogEvent> events) {
				for(LogEvent evt : events)
					try {
						System.out.write(
							String.format(sdf.format(new Date(evt.getTime())) + " [%s] %s: %s\r\n",
								evt.getLevel(),
								evt.getSource(),
								evt.getMessage()).getBytes()
							);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
			}
		}.execute();
	}
	
	@Override
	public synchronized void log(LogEvent evt)
	{
		queue.offer(evt);
		for(Logger logger : loggers)
			logger.log(evt);
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
	public synchronized void log(String message, Level level)
	{
		log(new ImplEvent(message, level));
	}
}