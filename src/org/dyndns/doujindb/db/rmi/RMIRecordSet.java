package org.dyndns.doujindb.db.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Iterator;

public interface RMIRecordSet<T> extends Remote
{
	public boolean contains(Object o) throws RemoteException;
	public int size() throws RemoteException;
	public Iterator<T> iterator() throws RemoteException;
}