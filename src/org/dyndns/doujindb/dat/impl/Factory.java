package org.dyndns.doujindb.dat.impl;

import java.io.File;

import org.dyndns.doujindb.dat.*;

/**  
* Factory.java - DataStore service factory.
* @author  nozomu
* @version 1.0
*/
public final class Factory
{
	public static DataStore getService(String spec) throws Exception
	{
		return new DataStoreImpl(new File(spec));
	}
}
