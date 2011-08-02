package org.dyndns.doujindb.db.containers;

import java.util.Set;

import org.dyndns.doujindb.db.records.Parody;

/**  
* ParodyContainer.java - Interface every item in the DB containing parody(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface ParodyContainer
{
	public Set<Parody> getParodies();
}
