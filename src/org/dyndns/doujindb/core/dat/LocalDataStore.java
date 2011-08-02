package org.dyndns.doujindb.core.dat;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;

import org.dyndns.doujindb.dat.*;

/** 
* LocalDataStore.java - DataStore on a local disk.
* @author  nozomu
* @version 1.0
*/
final class LocalDataStore implements DataStore
{
	private File Root ;
	
	LocalDataStore(File root)
	{
		Root = root;
	}
	
	@Override
	public Set<DataSource> children() throws DataStoreException
	{
		Set<DataSource> ds = new TreeSet<DataSource>();
		if(Root.listFiles() == null)
			return ds;
		for(File child : Root.listFiles())
			ds.add(new ImplDataSource(child));
		return ds;
	}

	@Override
	public DataSource child(String name) throws DataStoreException
	{
		File file = new File(Root, name);
		return new ImplDataSource(file);
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
	
	private final class ImplDataSource implements DataSource, Comparable<DataSource>
	{
		private File DsFile;
		
		public ImplDataSource(File file)
		{
			DsFile = file;
		}
		
		@Override
		public String getName() throws DataStoreException
		{
			if(!DsFile.equals(Root))
				return DsFile.getName();
			else
				return "/";
		}
		
		@Override
		public String getPath() throws DataStoreException
		{
			if(!DsFile.equals(Root))
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
				ds.add(new ImplDataSource(child));
			return ds;
		}
		
		@Override
		public DataSource child(String name) throws DataStoreException
		{
			File file = new File(DsFile, name);
			return new ImplDataSource(file);
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
			if(!DsFile.equals(Root))
				getParent().mkdirs();
			mkdir();
		}

		@Override
		public void delete() throws DataStoreException
		{
			if(!DsFile.equals(Root))
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
			if(!DsFile.equals(Root))
				return new ImplDataSource(DsFile.getParentFile());
			else
				return new ImplDataSource(Root);
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
