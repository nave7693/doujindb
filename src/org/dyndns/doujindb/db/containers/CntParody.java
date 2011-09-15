package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* CntParody.java - Interface every item in the DB containing parody(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface CntParody
{
	public RecordSet<Parody> getParodies() throws DataBaseException;
}
