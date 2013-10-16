package org.dyndns.doujindb.util;

import java.util.*;

/**	 
*	Hash.java - Hash methods.
*	@author  nozomu
*	@version 1.0
*/
public final class Hash
{
	public static String getUUID()
    {
		return getUUID(new TreeSet<String>());
    }
	
    public static String getUUID(Set<String> uuids)
    {
    	String uuid;
		generate_uuid:
		{
			while(true)
			{
				uuid = "{" + java.util.UUID.randomUUID().toString() + "}";
				if(!uuids.contains(uuid))
					break generate_uuid;
			}
		}
    	return uuid;
    }
}
