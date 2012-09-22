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
public class RemoteDataFile implements DataFile, Serializable, Comparable<DataFile>
{
	
	private RMIDataFile remoteDataSource;
	
	public RemoteDataFile(RMIDataFile remoteDataSource) throws RemoteException
	{
		this.remoteDataSource = remoteDataSource;
	}
	
	@Override
	public String getName() throws RepositoryException
	{
		try {
			return remoteDataSource.getName();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}
	
	@Override
	public String getPath() throws RepositoryException
	{
		try {
			return remoteDataSource.getPath();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public boolean isDirectory() throws RepositoryException
	{
		try {
			return remoteDataSource.isDirectory();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public boolean isFile() throws RepositoryException
	{
		try {
			return remoteDataSource.isFile();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public long size() throws RepositoryException
	{
		try {
			return remoteDataSource.size();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public InputStream getInputStream() throws RepositoryException
	{
		try {
			return remoteDataSource.getInputStream();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public OutputStream getOutputStream() throws RepositoryException
	{
		try {
			return remoteDataSource.getOutputStream();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public Set<DataFile> children() throws RepositoryException
	{
		try {
			return remoteDataSource.children();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}
	
	@Override
	public DataFile child(String name) throws RepositoryException
	{
		try {
			return remoteDataSource.child(name);
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public void touch() throws RepositoryException
	{
		try {
			remoteDataSource.touch();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}
	
	@Override
	public void mkdir() throws RepositoryException
	{
		try {
			remoteDataSource.mkdir();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}
	
	@Override
	public void mkdirs() throws RepositoryException
	{
		try {
			remoteDataSource.mkdirs();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public void delete() throws RepositoryException
	{
		try {
			remoteDataSource.delete();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public boolean exists() throws RepositoryException
	{
		try {
			return remoteDataSource.exists();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public DataFile getParent() throws RepositoryException
	{
		try {
			return remoteDataSource.getParent();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public boolean canRead() throws RepositoryException
	{
		try {
			return remoteDataSource.canRead();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public boolean canWrite() throws RepositoryException
	{
		try {
			return remoteDataSource.canWrite();
		} catch (RemoteException re) {
			throw new RepositoryException("RemoteException " + re);
		}
	}

	@Override
	public int compareTo(DataFile ds)
	{
		try {
			return remoteDataSource.compareTo(ds);
		} catch (RemoteException re) {
			return -1;
		}
	}
}
