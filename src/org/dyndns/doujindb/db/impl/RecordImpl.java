package org.dyndns.doujindb.db.impl;

import java.io.Serializable;

import org.apache.cayenne.CayenneDataObject;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;

@SuppressWarnings("serial")
abstract class RecordImpl implements Record, Serializable, Comparable<Record>
{
	protected CayenneDataObject ref;

	public RecordImpl() throws DataBaseException
	{
		super();
	}

//	@Override
//	public synchronized String getID() throws DataBaseException
//	{
//		if(ref.getObjectId().isTemporary())
//			return String.format("TMP-%08x", (int)(Math.random() * 0xffff));
//		//Can't get primary key from temporary id.
//		else
//			return String.format("%08x", DataObjectUtils.intPKForObject(ref));
//	}

	@Override
	public synchronized int compareTo(Record o)
	{
		return this.getID().compareTo(o.getID());
	}

	@Override
	public synchronized boolean equals(Object o)
	{
		if(o instanceof Record)
			return compareTo((Record)o) == 0;
		else
			return false;
	}
}
