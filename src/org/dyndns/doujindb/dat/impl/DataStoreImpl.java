package org.dyndns.doujindb.dat.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.TreeSet;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.dat.rmi.*;
import org.dyndns.doujindb.log.Level;

/** 
* DataStoreImpl.java - DataStore on a local disk.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class DataStoreImpl implements DataStore, Serializable
{
	private final String METADATA = ".metadata";
	private final String PREVIEW = ".preview";
	
	private File DsRoot;
	
	public DataStoreImpl(File root) throws RemoteException
	{
		this.DsRoot = root;
	}
	
	@Override
	public Set<DataSource> children() throws DataStoreException
	{
		Set<DataSource> ds = new TreeSet<DataSource>();
		if(DsRoot.listFiles() == null)
			return ds;
		for(File child : DsRoot.listFiles())
			try {
				ds.add(new RemoteDataSource(new RMIDataSourceImpl(new DataSourceImpl(child))));
			} catch (RemoteException re) {
				throw new DataStoreException(re);
			}
		return ds;
	}

	@Override
	public DataSource child(String name) throws DataStoreException
	{
		File file = new File(DsRoot, name);
		try {
			return new RemoteDataSource(new RMIDataSourceImpl(new DataSourceImpl(file)));
		} catch (RemoteException re) {
			throw new DataStoreException(re);
		}
	}
	
	@Override
	public long size() throws DataStoreException
	{
		long size = 0;
		for(DataSource ds : children())
		{
			if(ds.isDirectory())
				size += _size(ds);
			else
				size += ds.size();
		}
		return size;
	}
	
	private long _size(DataSource source) throws DataStoreException
	{
		long size = 0;
		for(DataSource ds : source.children())
		{
			if(ds.isDirectory())
				size += _size(ds);
			else
				size += ds.size();
		}
		return size;
	}
	
	@Override
	public DataSource getMetadata(String ID) throws DataStoreException
	{
		File file = new File(DsRoot, METADATA);
		try {
			return new RemoteDataSource(new RMIDataSourceImpl(new DataSourceImpl(file)));
		} catch (RemoteException re) {
			throw new DataStoreException(re);
		}
	}

	@Override
	public DataSource getPreview(String ID) throws DataStoreException
	{
		File file = new File(DsRoot, PREVIEW);
		try {
			return new RemoteDataSource(new RMIDataSourceImpl(new DataSourceImpl(file)));
		} catch (RemoteException re) {
			throw new DataStoreException(re);
		}
	}
	
	private final class DataSourceImpl implements DataSource, Comparable<DataSource>
	{
		private File DsFile;
		
		public DataSourceImpl(File file) throws DataStoreException
		{
			DsFile = file;
		}
		
		@Override
		public String getName() throws DataStoreException
		{
			if(!DsFile.equals(DsRoot))
				return DsFile.getName();
			else
				return "/";
		}
		
		@Override
		public String getPath() throws DataStoreException
		{
			if(!DsFile.equals(DsRoot))
				return getParent().getPath() + DsFile.getName() + (isDirectory()?"/":"");
			else
				return getName();
		}

		@Override
		public boolean isDirectory() throws DataStoreException
		{
			return DsFile.isDirectory();
		}

		@Override
		public boolean isFile() throws DataStoreException
		{
			return DsFile.isFile();
		}

		@Override
		public long size() throws DataStoreException
		{
			if(isDirectory())
				return -1L;
			else
				return DsFile.length();
		}

		@Override
		public InputStream getInputStream() throws DataStoreException
		{
			if(isDirectory())
				return null;
			try
			{
				return new RemoteInputStream(new RMIInputStreamImpl(new FileInputStream(DsFile)));
				//return new FileInputStream(DsFile);
			} catch (FileNotFoundException fnfe) {
				throw new DataStoreException(fnfe);
			} catch (RemoteException re) {
				throw new DataStoreException(re);
			}
		}

		@Override
		public OutputStream getOutputStream() throws DataStoreException
		{
			if(isDirectory())
				return null;
			try
			{
				return new RemoteOutputStream(new RMIOutputStreamImpl(new FileOutputStream(DsFile)));
				//return new FileOutputStream(DsFile);
			} catch (FileNotFoundException fnfe) {
				throw new DataStoreException(fnfe);
			} catch (RemoteException re) {
				throw new DataStoreException(re);
			}
		}

		@Override
		public Set<DataSource> children() throws DataStoreException
		{
			Set<DataSource> ds = new TreeSet<DataSource>();
			if(DsFile.listFiles() == null)
				return ds;
			for(File child : DsFile.listFiles())
				try {
					ds.add(new RemoteDataSource(new RMIDataSourceImpl(new DataSourceImpl(child))));
				} catch (RemoteException re) {
					throw new DataStoreException(re);
				}
			return ds;
		}
		
		@Override
		public DataSource child(String name) throws DataStoreException
		{
			File file = new File(DsFile, name);
			try {
				return new RemoteDataSource(new RMIDataSourceImpl(new DataSourceImpl(file)));
			} catch (RemoteException re) {
				throw new DataStoreException(re);
			}
		}

		@Override
		public void touch() throws DataStoreException
		{
			try {
				DsFile.createNewFile();
			} catch (IOException ioe) {
				throw new DataStoreException(ioe);
			}
		}
		
		@Override
		public void mkdir() throws DataStoreException
		{
			if(!DsFile.mkdir())
				if(!DsFile.exists())
					throw new DataStoreException("Could not create directory '" + getName()+ "'.");
		}
		
		@Override
		public void mkdirs() throws DataStoreException
		{
			if(!DsFile.equals(DsRoot))
				getParent().mkdirs();
			mkdir();
		}

		@Override
		public void delete() throws DataStoreException
		{
			if(!DsFile.equals(DsRoot))
			if(isDirectory())
			{
				_delete(children());
				if(!DsFile.delete())
					DsFile.deleteOnExit();
			}
			else	
			if(!DsFile.delete())
				DsFile.deleteOnExit();
		}
		
		private void _delete(Set<DataSource> dss) throws DataStoreException
		{
			for(DataSource ds : dss)
				if(ds.isDirectory())
				{
					_delete(ds.children());
					ds.delete();
				}
				else
					ds.delete();
		}

		@Override
		public int compareTo(DataSource ds)
		{
			try {
				return getName().compareTo(ds.getName());
			} catch (DataStoreException dse) {
				Core.Logger.log(dse.getMessage(), Level.ERROR);
				dse.printStackTrace();
			}
			return -2;
		}

		@Override
		public boolean exists() throws DataStoreException
		{
			return DsFile.exists();
		}

		@Override
		public DataSource getParent() throws DataStoreException
		{
			if(!DsFile.equals(DsRoot))
				try {
					return new RemoteDataSource(new RMIDataSourceImpl(new DataSourceImpl(DsFile.getParentFile())));
				} catch (RemoteException re) {
					throw new DataStoreException(re);
				}
			else
				try {
					return new RemoteDataSource(new RMIDataSourceImpl(new DataSourceImpl(DsFile)));
				} catch (RemoteException re) {
					throw new DataStoreException(re);
				}
		}

		@Override
		public boolean canRead() throws DataStoreException
		{
			return DsFile.canRead();
		}

		@Override
		public boolean canWrite() throws DataStoreException
		{
			return DsFile.canWrite();
		}
	}
}
