package org.dyndns.doujindb.db.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIIterator<E> extends Remote
{
	public boolean hasNext() throws RemoteException;
	public E next() throws RemoteException;
	public void remove() throws RemoteException;
}
