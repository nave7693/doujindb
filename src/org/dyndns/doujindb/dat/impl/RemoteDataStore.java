package org.dyndns.doujindb.dat.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Set;

import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.dat.rmi.*;

/** 
* RemoteDataStore.java - DataStore on a remote disk.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RemoteDataStore implements DataStore, Serializable
{
	
	private RMIDataStore remoteDataStore;
	
	public RemoteDataStore(RMIDataStore remoteDataStore) throws RemoteException
	{
		this.remoteDataStore = remoteDataStore;
	}
	
	@Override
	public Set<DataSource> children() throws DataStoreException
	{
		try {
			return remoteDataStore.children();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public DataSource child(String name) throws DataStoreException
	{
		try {
			return remoteDataStore.child(name);
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}
	
	@Override
	public long size() throws DataStoreException
	{
		try {
			return remoteDataStore.size();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}
	
	@Override
	public DataSource getMetadata(String ID) throws DataStoreException
	{
		try {
			return remoteDataStore.getMetadata(ID);
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public DataSource getPreview(String ID) throws DataStoreException
	{
		try {
			return remoteDataStore.getPreview(ID);
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}
}
