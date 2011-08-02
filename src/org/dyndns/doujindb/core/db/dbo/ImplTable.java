package org.dyndns.doujindb.core.db.dbo;

import java.io.Serializable;
import java.util.*;

import org.dyndns.doujindb.db.*;


public class ImplTable<T extends Record> implements Table<T>, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long index = 0L;
	private Hashtable<Long,T> records = new Hashtable<Long,T>();
	
	@Override
	public void insert(T row)
	{
		if(records.containsValue(row))
			return;
		index++;
		row.setID(index);
		records.put(index, row);
	}

	@Override
	public void delete(T row)
	{
		if(records.containsValue(row))
			records.values().remove(row);
	}

	@Override
	public boolean contains(T row)
	{
		if(row == null)
			return false;
		return records.containsValue(row);
	}

	@Override
	public long count()
	{
		return records.size();
	}

	@Override
	public Iterator<T> iterator()
	{
		Vector<T> v = new Vector<T>();
		for(T item : records.values())
			v.add(item);
		return v.iterator();
	}

}
