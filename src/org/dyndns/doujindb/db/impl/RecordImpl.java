package org.dyndns.doujindb.db.impl;

import java.io.Serializable;

import org.dyndns.doujindb.db.Record;


@SuppressWarnings("serial")
abstract class RecordImpl implements Record, Serializable, Comparable<Record>
{
	long ID;
	
	public RecordImpl()
	{
		ID = -1L;
	}
	
	@Override
	public synchronized void setID(long id)
	{
		ID = id;
	}
	
	@Override
	public synchronized int compareTo(Record o)
	{
		if(this.getID() == null)
			if(o.getID() == null)
				return 0;
			else
				return -1;
		if(o.getID() == null)
			if(this.getID() == null)
				return 0;
			else
				return -1;
		return this.getID().compareTo(o.getID());
	}
}
