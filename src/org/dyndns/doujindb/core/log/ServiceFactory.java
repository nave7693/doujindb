package org.dyndns.doujindb.core.log;

import java.io.*;

import org.dyndns.doujindb.log.*;

/**  
* ServiceFactory.java - Logger service factory.
* @author  nozomu
* @version 1.0
*/
public final class ServiceFactory
{
	public static Logger getService(String spec) throws Exception
	{
		if(spec.equals("stdout://"))
			return new StdOutLogger();
		if(spec.startsWith("file://"))
			return new FileLogger(new File(new java.net.URL(spec).toURI()));
		throw new Exception("Invalid spec provided '" + spec + "'.");
	}
}
