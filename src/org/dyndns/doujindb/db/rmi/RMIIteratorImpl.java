package org.dyndns.doujindb.db.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

@SuppressWarnings("serial")
public final class RMIIteratorImpl<E> extends UnicastRemoteObject implements RMIIterator<E>
{
	private Iterator<E> i;
	
	protected RMIIteratorImpl(Iterator<E> i) throws RemoteException
	{
		super(1099);
		this.i = i;
	}

	@Override
	public boolean hasNext() throws RemoteException {
		return i.hasNext();
	}

	@Override
	public E next() throws RemoteException {
		return i.next();
	}

	@Override
	public void remove() throws RemoteException {
		i.remove();
	}
}
