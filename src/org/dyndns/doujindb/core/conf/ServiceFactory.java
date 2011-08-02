package org.dyndns.doujindb.core.conf;

import org.dyndns.doujindb.conf.*;

/**  
* ServiceFactory.java - DataStore service factory.
* @author  nozomu
* @version 1.0
*/
public final class ServiceFactory
{
	public static Properties getService(String spec) throws Exception
	{
		return new SerializedProperties();
		/*
		if(spec == null)
			return new SerializedProperties();
		throw new Exception("Invalid spec provided '" + spec + "'.");
		*/
	}
}
