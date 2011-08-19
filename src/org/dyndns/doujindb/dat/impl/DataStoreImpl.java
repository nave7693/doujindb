package org.dyndns.doujindb.dat.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.TreeSet;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.*;
import org.dyndns.doujindb.log.Level;

/** 
* DataStoreImpl.java - DataStore on a local disk.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class DataStoreImpl extends UnicastRemoteObject implements DataStore
{
	private final String METADATA = ".metadata";
	private final String PREVIEW = ".preview";
	
	private File DsRoot;
	
	public DataStoreImpl(File root) throws RemoteException
	{
		this.DsRoot = root;
	}
	
	@Override
	public Set<DataSource> children() throws DataStoreException, RemoteException
	{
		Set<DataSource> ds = new TreeSet<DataSource>();
		if(DsRoot.listFiles() == null)
			return ds;
		for(File child : DsRoot.listFiles())
			ds.add(new DataSourceImpl(child));
		return ds;
	}

	@Override
	public DataSource child(String name) throws DataStoreException, RemoteException
	{
		File file = new File(DsRoot, name);
		return new DataSourceImpl(file);
	}
	
	@Override
	public long size() throws DataStoreException, RemoteException
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
	
	private long _size(DataSource source) throws DataStoreException, RemoteException
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
	public DataSource getMetadata(String ID) throws DataStoreException, RemoteException
	{
		File file = new File(DsRoot, METADATA);
		return new DataSourceImpl(file);
	}

	@Override
	public DataSource getPreview(String ID) throws DataStoreException, RemoteException
	{
		File file = new File(DsRoot, PREVIEW);
		return new DataSourceImpl(file);
	}
	
	private final class DataSourceImpl extends UnicastRemoteObject implements DataSource, Comparable<DataSource>
	{
		private File DsFile;
		
		public DataSourceImpl(File file) throws RemoteException
		{
			DsFile = file;
		}
		
		@Override
		public String getName() throws DataStoreException, RemoteException
		{
			if(!DsFile.equals(DsRoot))
				return DsFile.getName();
			else
				return "/";
		}
		
		@Override
		public String getPath() throws DataStoreException, RemoteException
		{
			if(!DsFile.equals(DsRoot))
				return getParent().getPath() + DsFile.getName() + (isDirectory()?"/":"");
			else
				return getName();
		}

		@Override
		public boolean isDirectory() throws DataStoreException, RemoteException
		{
			return DsFile.isDirectory();
		}

		@Override
		public boolean isFile() throws DataStoreException, RemoteException
		{
			return DsFile.isFile();
		}

		@Override
		public long size() throws DataStoreException, RemoteException
		{
			if(isDirectory())
				return -1L;
			else
				return DsFile.length();
		}

		@Override
		public InputStream getInputStream() throws DataStoreException, RemoteException
		{
			if(isDirectory())
				return null;
			try
			{
				return new RemoteInputStream(new RMIInputStreamImpl(new FileInputStream(DsFile)));
				//return new FileInputStream(DsFile);
			} catch (FileNotFoundException e)
			{
				throw new DataStoreException(e);
			}
		}

		@Override
		public OutputStream getOutputStream() throws DataStoreException, RemoteException
		{
			if(isDirectory())
				return null;
			try
			{
				return new RemoteOutputStream(new RMIOutputStreamImpl(new FileOutputStream(DsFile)));
				//return new FileOutputStream(DsFile);
			} catch (FileNotFoundException e)
			{
				throw new DataStoreException(e);
			}
		}

		@Override
		public Set<DataSource> children() throws DataStoreException, RemoteException
		{
			Set<DataSource> ds = new TreeSet<DataSource>();
			if(DsFile.listFiles() == null)
				return ds;
			for(File child : DsFile.listFiles())
				ds.add(new DataSourceImpl(child));
			return ds;
		}
		
		@Override
		public DataSource child(String name) throws DataStoreException, RemoteException
		{
			File file = new File(DsFile, name);
			return new DataSourceImpl(file);
		}

		@Override
		public void touch() throws DataStoreException, RemoteException
		{
			try {
				DsFile.createNewFile();
			} catch (IOException ioe) {
				throw new DataStoreException(ioe);
			}
		}
		
		@Override
		public void mkdir() throws DataStoreException, RemoteException
		{
			if(!DsFile.mkdir())
				if(!DsFile.exists())
					throw new DataStoreException("Could not create directory '" + getName()+ "'.");
		}
		
		@Override
		public void mkdirs() throws DataStoreException, RemoteException
		{
			if(!DsFile.equals(DsRoot))
				getParent().mkdirs();
			mkdir();
		}

		@Override
		public void delete() throws DataStoreException, RemoteException
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
		
		private void _delete(Set<DataSource> dss) throws DataStoreException, RemoteException
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
			} catch (RemoteException re) {
				Core.Logger.log(re.getMessage(), Level.ERROR);
				re.printStackTrace();
			}
			return -2;
		}

		@Override
		public boolean exists() throws DataStoreException, RemoteException
		{
			return DsFile.exists();
		}

		@Override
		public DataSource getParent() throws DataStoreException, RemoteException
		{
			if(!DsFile.equals(DsRoot))
				return new DataSourceImpl(DsFile.getParentFile());
			else
				return new DataSourceImpl(DsRoot);
		}

		@Override
		public boolean canRead() throws DataStoreException, RemoteException
		{
			return DsFile.canRead();
		}

		@Override
		public boolean canWrite() throws DataStoreException, RemoteException
		{
			return DsFile.canWrite();
		}
	}
}
