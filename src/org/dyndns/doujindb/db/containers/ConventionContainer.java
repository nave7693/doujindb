package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Convention;

/**  
* CntConvention.java - Interface every item in the DB containing convention(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface ConventionContainer
{
	public Convention getConvention() throws DataBaseException;
	public void setConvention(Convention convention) throws DataBaseException;
}
