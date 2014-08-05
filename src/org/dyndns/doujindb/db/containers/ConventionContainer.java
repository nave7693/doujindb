package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Convention;

public interface ConventionContainer
{
	public Convention getConvention() throws DataBaseException;
	public void setConvention(Convention convention) throws DataBaseException;
}
