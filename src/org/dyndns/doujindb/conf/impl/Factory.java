package org.dyndns.doujindb.conf.impl;

import org.dyndns.doujindb.conf.*;

/**  
* Factory.java - DataStore service factory.
* @author  nozomu
* @version 1.0
*/
public final class Factory
{
	public static Properties getService(String spec) throws Exception
	{
		return new XMLProperties();
		/*
		if(spec == null)
			return new SerializedProperties();
		throw new Exception("Invalid spec provided '" + spec + "'.");
		*/
	}
}