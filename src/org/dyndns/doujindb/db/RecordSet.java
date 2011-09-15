package org.dyndns.doujindb.db;

public interface RecordSet<T> extends Iterable<T>
{
//	public boolean add(T e);
//	public boolean addAll(Collection<? extends T> c);
//	public void clear();
	public boolean contains(Object o) throws DataBaseException;
//	public boolean containsAll(Collection<?> c);
//	public boolean isEmpty();
//	public Iterator<T> iterator();
//	public boolean remove(Object o);
//	public boolean removeAll(Collection<?> c);
//	public boolean retainAll(Collection<?> c);
	public int size() throws DataBaseException;
//	public Object[] toArray();
//	public T[] toArray(T[] a);
	
}
