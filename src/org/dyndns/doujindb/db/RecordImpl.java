package org.dyndns.doujindb.db;

import java.io.Serializable;

import org.apache.cayenne.CayenneDataObject;

@SuppressWarnings("serial")
abstract class RecordImpl implements Record, Serializable
{
	public RecordImpl() throws DataBaseException
	{
		super();
	}
	
	abstract protected CayenneDataObject getRef();
}
