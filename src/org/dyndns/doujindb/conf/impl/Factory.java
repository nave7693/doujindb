package org.dyndns.doujindb.conf.impl;

import org.dyndns.doujindb.conf.*;

/**  
* Factory.java - Repository service factory.
* @author  nozomu
* @version 1.0
*/
public final class Factory
{
	public static Properties getService() throws Exception
	{
		return new XMLProperties();
	}
}
