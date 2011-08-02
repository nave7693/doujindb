package org.dyndns.doujindb.db.containers;

import java.util.Set;

import org.dyndns.doujindb.db.records.Content;

/**  
* ContentContainer.java - Interface every item in the DB containing content(s) [ok, i lol'd] must implement.
* @author nozomu
* @version 1.0
*/
public interface ContentContainer
{
	public Set<Content> getContents();
}
