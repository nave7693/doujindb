package org.dyndns.doujindb.db.impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import org.dyndns.doujindb.db.*;

final class TableImpl<T extends Record> extends UnicastRemoteObject implements Table<T>, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long index = 0L;
	private Hashtable<Long,T> records = new Hashtable<Long,T>();
	
	protected TableImpl() throws RemoteException
	{
		super(1099);
	}
	
	@Override
	public synchronized void insert(T row) throws RemoteException
	{
		if(records.containsValue(row))
			return;
		index++;
		//row.setID(index);
		records.put(index, row);
	}

	@Override
	public synchronized void delete(T row) throws RemoteException
	{
		if(records.containsValue(row))
			records.values().remove(row);
	}

	@Override
	public synchronized boolean contains(T row) throws RemoteException
	{
		if(row == null)
			return false;
		return records.containsValue(row);
	}

	@Override
	public synchronized long count() throws RemoteException
	{
		return records.size();
	}

	@Override
	public synchronized Iterable<T> elements() throws RemoteException
	{
		/* FIX
		 * throws java.io.NotSerializableException: java.util.Hashtable$ValueCollection with RMI
		 * return records.values();
		 */
		Vector<T> buff = new Vector<T>();
		buff.addAll(records.values());
		return buff;
	}

}
