package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

public interface ContentContainer
{
	public RecordSet<Content> getContents() throws DataBaseException;
	public void addContent(Content content) throws DataBaseException;
	public void removeContent(Content content) throws DataBaseException;
}
