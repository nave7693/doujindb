package org.dyndns.doujindb.db.container;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.record.Convention;

public interface ConventionContainer
{
	public Convention getConvention() throws DataBaseException;
	public void setConvention(Convention convention) throws DataBaseException;
}
