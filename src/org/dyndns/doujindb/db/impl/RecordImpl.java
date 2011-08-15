package org.dyndns.doujindb.db.impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.dyndns.doujindb.db.Record;


@SuppressWarnings("serial")
abstract class RecordImpl extends UnicastRemoteObject implements Record, Serializable, Comparable<Record>
{
	long ID;
	
	public RecordImpl() throws RemoteException
	{
		ID = -1L;
	}
	
	@Override
	public synchronized void setID(long id) throws RemoteException
	{
		ID = id;
	}
	
	@Override
	public synchronized int compareTo(Record o)
	{
		try {
			if(this.getID() == null)
				if(o.getID() == null)
					return 0;
				else
					return -1;
		if(o.getID() == null)
			if(this.getID() == null)
				return 0;
			else
				return -1;
		return this.getID().compareTo(o.getID());
		} catch (RemoteException re) {
			re.printStackTrace();
			return -2;
		}
	}
}
