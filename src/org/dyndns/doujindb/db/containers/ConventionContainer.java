package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.records.Convention;

/**  
* ConventionContainer.java - Interface every item in the DB containing convention(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface ConventionContainer
{
	public Convention getConvention();
	public void setConvention(Convention convention);
}
