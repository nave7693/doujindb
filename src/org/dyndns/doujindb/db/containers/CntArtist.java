package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* CntArtist.java - Interface every item in the DB containing artist(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface CntArtist
{
	public RecordSet<Artist> getArtists() throws DataBaseException;
}
