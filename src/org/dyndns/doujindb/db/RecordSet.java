package org.dyndns.doujindb.db;

import java.io.Serializable;

public interface RecordSet<T> extends Serializable, Iterable<T>
{
//	public boolean add(T e);
//	public boolean addAll(Collection<? extends T> c);
//	public void clear();
	public boolean contains(Object o);
//	public boolean containsAll(Collection<?> c);
//	public boolean isEmpty();
//	public Iterator<T> iterator();
//	public boolean remove(Object o);
//	public boolean removeAll(Collection<?> c);
//	public boolean retainAll(Collection<?> c);
	public int size();
//	public Object[] toArray();
//	public T[] toArray(T[] a);
	
}
