package org.dyndns.doujindb.dat.impl;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;

import org.dyndns.doujindb.dat.*;

/** 
* DataStoreImpl.java - DataStore on a local disk.
* @author  nozomu
* @version 1.0
*/
final class DataStoreImpl implements DataStore
{
	private final String METADATA = ".metadata";
	private final String PREVIEW = ".preview";
	
	private File DsRoot;
	
	DataStoreImpl(File root)
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
			ds.add(new DataSourceImpl(child));
		return ds;
	}

	@Override
	public DataSource child(String name) throws DataStoreException
	{
		File file = new File(DsRoot, name);
		return new DataSourceImpl(file);
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
	
	private long _size(DataSource source)
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
		return new DataSourceImpl(file);
	}

	@Override
	public DataSource getPreview(String ID) throws DataStoreException
	{
		File file = new File(DsRoot, PREVIEW);
		return new DataSourceImpl(file);
	}
	
	private final class DataSourceImpl implements DataSource, Comparable<DataSource>
	{
		private File DsFile;
		
		public DataSourceImpl(File file)
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
				return new FileInputStream(DsFile);
			} catch (FileNotFoundException e)
			{
				throw new DataStoreException(e);
			}
		}

		@Override
		public OutputStream getOutputStream() throws DataStoreException
		{
			if(isDirectory())
				return null;
			try
			{
				return new FileOutputStream(DsFile);
			} catch (FileNotFoundException e)
			{
				throw new DataStoreException(e);
			}
		}

		@Override
		public Set<DataSource> children() throws DataStoreException
		{
			Set<DataSource> ds = new TreeSet<DataSource>();
			if(DsFile.listFiles() == null)
				return ds;
			for(File child : DsFile.listFiles())
				ds.add(new DataSourceImpl(child));
			return ds;
		}
		
		@Override
		public DataSource child(String name) throws DataStoreException
		{
			File file = new File(DsFile, name);
			return new DataSourceImpl(file);
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
			return getName().compareTo(ds.getName());
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
				return new DataSourceImpl(DsFile.getParentFile());
			else
				return new DataSourceImpl(DsRoot);
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
