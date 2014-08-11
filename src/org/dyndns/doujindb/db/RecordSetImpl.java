package org.dyndns.doujindb.db;

import java.util.*;

final class RecordSetImpl<T extends Record> implements RecordSet<T>
{
	private Set<T> set;
	
	protected RecordSetImpl()
	{
		set = new TreeSet<T>();
	}
	
	protected RecordSetImpl(Set<T> data)
	{
		this();
		set.addAll(data); //FIXME This kills query pagination
	}
	
	protected RecordSetImpl(List<T> data)
	{
		this();
		set.addAll(data); //FIXME This kills query pagination
	}

	@Override
	public int size()
	{
		return set.size();
	}

	@Override
	public synchronized Iterator<T> iterator()
	{
		return set.iterator();
	}
}
