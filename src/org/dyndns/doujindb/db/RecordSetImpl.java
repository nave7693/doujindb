package org.dyndns.doujindb.db;

import java.util.*;

final class RecordSetImpl<T extends Record> implements RecordSet<T>
{
	private Iterator<T> data;
	private int size;
	
	protected RecordSetImpl(Set<T> data)
	{
		this.data = data.iterator();
		this.size = data.size();
	}
	
	protected RecordSetImpl(List<T> data)
	{
		this.data = data.iterator();
		this.size = data.size();
	}
	
	protected RecordSetImpl(Iterable<T> data, int size)
	{
		this.data = data.iterator();
		this.size = size;
	}
	
	protected RecordSetImpl(Iterator<T> data, int size)
	{
		this.data = data;
		this.size = size;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public synchronized Iterator<T> iterator()
	{
		return data;
	}
}
