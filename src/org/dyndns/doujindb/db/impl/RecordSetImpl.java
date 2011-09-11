package org.dyndns.doujindb.db.impl;

import java.util.*;

import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.RecordSet;

@SuppressWarnings("serial")
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
		set.addAll(data);
	}
	
	protected RecordSetImpl(List<T> data)
	{
		this();
		set.addAll(data);
	}

//	@Override
//	public boolean add(T e)
//	{
//		return set.add(e);
//	}
//
//	@Override
//	public boolean addAll(Collection<? extends T> c)
//	{
//		return set.addAll(c);
//	}
//
//	@Override
//	public void clear()
//	{
//		set.clear();
//	}

	@Override
	public boolean contains(Object o)
	{
		if(o == null)
			return false;
		return set.contains(o);
	}

//	@Override
//	public boolean containsAll(Collection<?> c)
//	{
//		return set.containsAll(c);
//	}
//
//	@Override
//	public boolean isEmpty()
//	{
//		return set.isEmpty();
//	}
//
//	@Override
//	public Iterator<T> iterator()
//	{
//		return set.iterator();
//	}
//
//	@Override
//	public boolean remove(Object o)
//	{
//		return set.remove(o);
//	}
//
//	@Override
//	public boolean removeAll(Collection<?> c)
//	{
//		return set.removeAll(c);
//	}
//
//	@Override
//	public boolean retainAll(Collection<?> c)
//	{
//		return set.retainAll(c);
//	}

	@Override
	public int size()
	{
		return set.size();
	}

//	@Override
//	public Object[] toArray()
//	{
//		return set.toArray();
//	}
//
//	@Override
//	public T[] toArray(T[] a)
//	{
//		return set.toArray(a);
//	}
//	
//	@Override
//	public synchronized Iterable<T> elements()
//	{
//		Vector<T> buff = new Vector<T>();
//		for(T i : set)
//			buff.add(i);
//		return buff;
//	}
	
	@Override
	public synchronized Iterator<T> iterator()
	{
		return set.iterator();
	}
	
	@Override
	public synchronized String toString()
	{
		String string = "[";
		for(T i : set)
			string += i + ",";
		if(string.endsWith(","))
			string = string.substring(0, string.length()-1);
		return string + "]";
	}
}
