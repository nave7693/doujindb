package org.dyndns.doujindb.db.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

import org.dyndns.doujindb.db.RecordSet;

@SuppressWarnings("serial")
public final class RMIRecordSetImpl<T> extends UnicastRemoteObject implements RMIRecordSet<T>
{
	private RecordSet<T> recordSet;
	
	protected RMIRecordSetImpl(RecordSet<T> recordSet) throws RemoteException
	{
		super(1099);
		this.recordSet = recordSet;
	}

	@Override
	public boolean contains(Object o) throws RemoteException {
		return recordSet.contains(o);
	}

	@Override
	public int size() throws RemoteException {
		return recordSet.size();
	}

	@Override
	public Iterator<T> iterator() throws RemoteException {
		return null;
	}
}