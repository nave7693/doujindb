package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* CntCircle.java - Interface every item in the DB containing circle(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface CircleContainer
{
	public RecordSet<Circle> getCircles() throws DataBaseException;
}
