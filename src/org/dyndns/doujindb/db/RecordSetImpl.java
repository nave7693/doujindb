package org.dyndns.doujindb.db;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
final class RecordSetImpl<T extends Record> implements RecordSet<T>, Serializable
{
	private Set<T> set;
	
	protected RecordSetImpl()
	{
		set = new TreeSet<T>();
	}
	
	protected RecordSetImpl(Set<T> data)
	{
		this();
		set.addAll(data);
	}
	
	protected RecordSetImpl(List<T> data)
	{
		this();
		set.addAll(data);
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
