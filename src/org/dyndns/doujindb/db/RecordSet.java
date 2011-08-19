package org.dyndns.doujindb.db;
import java.util.*;
import java.io.Serializable;
import java.rmi.*;

public interface RecordSet<T> extends Remote, Serializable
{
	public boolean add(T e) throws RemoteException;
	public boolean addAll(Collection<? extends T> c) throws RemoteException;
	public void clear() throws RemoteException;
	public boolean contains(Object o) throws RemoteException;
	public boolean containsAll(Collection<?> c) throws RemoteException;
	public boolean isEmpty() throws RemoteException;
	public Iterator<T> iterator() throws RemoteException;
	public boolean remove(Object o) throws RemoteException;
	public boolean removeAll(Collection<?> c) throws RemoteException;
	public boolean retainAll(Collection<?> c) throws RemoteException;
	public int size() throws RemoteException;
	public Object[] toArray() throws RemoteException;
	public T[] toArray(T[] a) throws RemoteException;
	public Iterable<T> elements() throws RemoteException;
	public String getString() throws RemoteException;
}
