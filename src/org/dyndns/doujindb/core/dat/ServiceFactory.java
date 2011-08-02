package org.dyndns.doujindb.core.dat;

import java.io.File;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.*;

/**  
* ServiceFactory.java - DataStore service factory.
* @author  nozomu
* @version 1.0
*/
public final class ServiceFactory
{
	public static DataStore getService(String spec) throws Exception
	{
		return new LocalDataStore(new File(Core.Properties.get("org.dyndns.doujindb.dat.datastore").asString()));
	}
}
