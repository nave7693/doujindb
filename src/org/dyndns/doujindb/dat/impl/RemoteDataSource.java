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
public class RemoteDataSource implements DataSource, Serializable, Comparable<DataSource>
{
	
	private RMIDataSource remoteDataSource;
	
	public RemoteDataSource(RMIDataSource remoteDataSource) throws RemoteException
	{
		this.remoteDataSource = remoteDataSource;
	}
	
	@Override
	public String getName() throws DataStoreException
	{
		try {
			return remoteDataSource.getName();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}
	
	@Override
	public String getPath() throws DataStoreException
	{
		try {
			return remoteDataSource.getPath();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public boolean isDirectory() throws DataStoreException
	{
		try {
			return remoteDataSource.isDirectory();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public boolean isFile() throws DataStoreException
	{
		try {
			return remoteDataSource.isFile();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public long size() throws DataStoreException
	{
		try {
			return remoteDataSource.size();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public InputStream getInputStream() throws DataStoreException
	{
		try {
			return remoteDataSource.getInputStream();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public OutputStream getOutputStream() throws DataStoreException
	{
		try {
			return remoteDataSource.getOutputStream();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public Set<DataSource> children() throws DataStoreException
	{
		try {
			return remoteDataSource.children();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}
	
	@Override
	public DataSource child(String name) throws DataStoreException
	{
		try {
			return remoteDataSource.child(name);
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public void touch() throws DataStoreException
	{
		try {
			remoteDataSource.touch();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}
	
	@Override
	public void mkdir() throws DataStoreException
	{
		try {
			remoteDataSource.mkdir();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}
	
	@Override
	public void mkdirs() throws DataStoreException
	{
		try {
			remoteDataSource.mkdirs();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public void delete() throws DataStoreException
	{
		try {
			remoteDataSource.delete();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public boolean exists() throws DataStoreException
	{
		try {
			return remoteDataSource.exists();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public DataSource getParent() throws DataStoreException
	{
		try {
			return remoteDataSource.getParent();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public boolean canRead() throws DataStoreException
	{
		try {
			return remoteDataSource.canRead();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public boolean canWrite() throws DataStoreException
	{
		try {
			return remoteDataSource.canWrite();
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}

	@Override
	public int compareTo(DataSource ds)
	{
		try {
			return remoteDataSource.compareTo(ds);
		} catch (RemoteException re) {
			throw new DataStoreException("RemoteException " + re);
		}
	}
}
