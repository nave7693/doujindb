package org.dyndns.doujindb.db;

import java.io.Serializable;

import org.apache.cayenne.CayenneDataObject;

@SuppressWarnings("serial")
abstract class RecordImpl implements Record, Serializable, Comparable<Record>
{
	protected CayenneDataObject ref;

	public RecordImpl() throws DataBaseException
	{
		super();
	}

	@Override
	public synchronized int compareTo(Record o)
	{
		if(getId() == null || o.getId() == null)
			return ((Integer)super.hashCode()).compareTo(o.hashCode());
		else
			return this.getId().compareTo(o.getId());
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
