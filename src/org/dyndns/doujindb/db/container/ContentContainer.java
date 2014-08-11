package org.dyndns.doujindb.db.container;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.record.*;

public interface ContentContainer
{
	public RecordSet<Content> getContents() throws DataBaseException;
	public void addContent(Content content) throws DataBaseException;
	public void removeContent(Content content) throws DataBaseException;
}
