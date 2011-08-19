package org.dyndns.doujindb.db.impl;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.RecordSet;

@SuppressWarnings("serial")
final class RecordSetImpl<T extends Record> extends UnicastRemoteObject implements RecordSet<T>
{
	private Set<T> set;
	
	protected RecordSetImpl() throws RemoteException {
		set = new HashSet<T>();
	}

	@Override
	public boolean add(T e) throws RemoteException {
		return set.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) throws RemoteException {
		return set.addAll(c);
	}

	@Override
	public void clear() throws RemoteException {
		set.clear();
	}

	@Override
	public boolean contains(Object o) throws RemoteException {
		return set.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) throws RemoteException {
		return set.containsAll(c);
	}

	@Override
	public boolean isEmpty() throws RemoteException {
		return set.isEmpty();
	}

	@Override
	public Iterator<T> iterator() throws RemoteException {
		return set.iterator();
	}

	@Override
	public boolean remove(Object o) throws RemoteException {
		return set.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) throws RemoteException {
		return set.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) throws RemoteException {
		return set.retainAll(c);
	}

	@Override
	public int size() throws RemoteException {
		return set.size();
	}

	@Override
	public Object[] toArray() throws RemoteException {
		return set.toArray();
	}

	@Override
	public T[] toArray(T[] a) throws RemoteException {
		return set.toArray(a);
	}
	
	@Override
	public synchronized Iterable<T> elements() throws RemoteException
	{
		Vector<T> buff = new Vector<T>();
		for(T i : set)
			buff.add(i);
		return buff;
	}
	
	@Override
	public String getString() throws RemoteException {
		String s = "[";
		for(T item : set)
			s += item.getString() + ",";
		s = s.substring(0, s.length()-1);
		return s + "]";
	}
}
