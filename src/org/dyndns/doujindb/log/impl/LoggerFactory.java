package org.dyndns.doujindb.log.impl;

import java.io.*;

import org.dyndns.doujindb.log.*;

/**  
* LoggerFactory.java - Logger service factory.
* @author  nozomu
* @version 1.0
*/
public final class LoggerFactory
{
	public enum Type
	{
		STDOUT,
		FILE
	}
	
	public static Logger getService(Type spec) throws Exception
	{
		switch(spec)
		{
		case STDOUT:
			return new StdOutLogger();
		case FILE:
			return new FileLogger(
					new File(
							new File(
									new File(System.getProperty("user.home"), ".doujindb")
									, "log")
							, "doujindb.out"));
		default:
			throw new Exception("Invalid spec provided '" + spec + "'.");
		}
	}
}
