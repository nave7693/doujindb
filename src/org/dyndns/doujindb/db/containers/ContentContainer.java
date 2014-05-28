package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* CntContent.java - Interface every item in the DB containing content(s) [ok, i lol'd] must implement.
* @author nozomu
* @version 1.0
*/
public interface ContentContainer
{
	public RecordSet<Content> getContents() throws DataBaseException;
	public void addContent(Content content) throws DataBaseException;
	public void removeContent(Content content) throws DataBaseException;
}
