package org.dyndns.doujindb.db;

/**  
* Matchable.java - Interface for Regular Expression matching.
* @author  nozomu
* @version 1.0
*/
public interface Matchable
{
	public boolean matches(String regex);
}
