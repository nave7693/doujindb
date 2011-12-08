package org.dyndns.doujindb.dat.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Set;

import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.dat.rmi.*;

/** 
* RemoteRepository.java - Repository on a remote disk.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RemoteRepository implements Repository, Serializable
{
	
	private RMIRepository remoteDataStore;
	
	public RemoteRepository(RMIRepository remoteDataStore) throws RemoteException
	{
		this.remoteDataStore = remoteDataStore;
	}
	
	@Override
	public Set<DataFile> children() throws RepositoryException
	{
		try {
			return remoteDataStore.children();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public DataFile child(String name) throws RepositoryException
	{
		try {
			return remoteDataStore.child(name);
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}
	
	@Override
	public long size() throws RepositoryException
	{
		try {
			return remoteDataStore.size();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}
	
	@Override
	public DataFile getMetadata(String ID) throws RepositoryException
	{
		try {
			return remoteDataStore.getMetadata(ID);
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public DataFile getPreview(String ID) throws RepositoryException
	{
		try {
			return remoteDataStore.getPreview(ID);
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}
}
