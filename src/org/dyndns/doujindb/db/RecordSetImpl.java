package org.dyndns.doujindb.db;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
final class RecordSetImpl<T extends Record> implements RecordSet<T>, Serializable
{
	private Set<T> set;
	
	protected RecordSetImpl() throws DataBaseException
	{
		set = new TreeSet<T>();
	}
	
	protected RecordSetImpl(Set<T> data) throws DataBaseException
	{
		this();
		set.addAll(data);
	}
	
	protected RecordSetImpl(List<T> data) throws DataBaseException
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
	public boolean contains(Object o) throws DataBaseException
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
	public int size() throws DataBaseException
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
