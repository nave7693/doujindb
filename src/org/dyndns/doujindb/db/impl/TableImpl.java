package org.dyndns.doujindb.db.impl;

import java.io.Serializable;
import java.util.*;

import org.dyndns.doujindb.db.*;

final class TableImpl<T extends Record> implements Table<T>, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long index = 0L;
	private Hashtable<Long,T> records = new Hashtable<Long,T>();
	
	@Override
	public synchronized void insert(T row)
	{
		if(records.containsValue(row))
			return;
		index++;
		row.setID(index);
		records.put(index, row);
	}

	@Override
	public synchronized void delete(T row)
	{
		if(records.containsValue(row))
			records.values().remove(row);
	}

	@Override
	public synchronized boolean contains(T row)
	{
		if(row == null)
			return false;
		return records.containsValue(row);
	}

	@Override
	public synchronized long count()
	{
		return records.size();
	}

	@Override
	public synchronized Iterator<T> iterator()
	{
		Vector<T> v = new Vector<T>();
		for(T item : records.values())
			v.add(item);
		return v.iterator();
	}

}
