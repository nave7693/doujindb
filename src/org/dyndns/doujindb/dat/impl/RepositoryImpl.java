package org.dyndns.doujindb.dat.impl;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.dat.*;
//import org.dyndns.doujindb.dat.rmi.*;
import org.dyndns.doujindb.log.Level;

/** 
* RepositoryImpl.java - Repository on a local disk.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RepositoryImpl implements Repository, Serializable
{
	private final String METADATA = ".xml";
	private final String PREVIEW = ".preview";
	
	private File DsRoot;
	
	public RepositoryImpl(File root)// throws RemoteException
	{
		this.DsRoot = root;
	}
	
	@Override
	public Set<DataFile> children() throws RepositoryException
	{
		Set<DataFile> ds = new TreeSet<DataFile>();
		if(DsRoot.listFiles() == null)
			return ds;
		for(File child : DsRoot.listFiles())
//			try {
//				ds.add(new RemoteDataFile(new RMIDataFileImpl(new DataSourceImpl(child))));
//			} catch (RemoteException re) {
//				throw new RepositoryException(re);
//			}
			ds.add(new DataSourceImpl(child));
		return ds;
	}

	@Override
	public DataFile child(String name) throws RepositoryException
	{
		if(!new File(DsRoot, name).getParentFile().equals(DsRoot))
			throw new RepositoryException("Specified file name '" + name + "' is not valid.");
		File file = new File(DsRoot, name);
//		try {
//			return new RemoteDataFile(new RMIDataFileImpl(new DataSourceImpl(file)));
//		} catch (RemoteException re) {
//			throw new RepositoryException(re);
//		}
		return new DataSourceImpl(file);
	}
	
	@Override
	public long size() throws RepositoryException
	{
		long size = 0;
		for(DataFile ds : children())
		{
			if(ds.isDirectory())
				size += _size(ds);
			else
				size += ds.size();
		}
		return size;
	}
	
	private long _size(DataFile source) throws RepositoryException
	{
		long size = 0;
		for(DataFile ds : source.children())
		{
			if(ds.isDirectory())
				size += _size(ds);
			else
				size += ds.size();
		}
		return size;
	}
	
	@Override
	public DataFile getMetadata(String ID) throws RepositoryException
	{
		return child(ID).child(METADATA);
	}

	@Override
	public DataFile getPreview(String ID) throws RepositoryException
	{
		return child(ID).child(PREVIEW);
	}
	
	private final class DataSourceImpl implements DataFile, Comparable<DataFile>
	{
		private File DsFile;
		
		public DataSourceImpl(File file) throws RepositoryException
		{
			DsFile = file;
		}
		
		@Override
		public String getName() throws RepositoryException
		{
			if(!DsFile.equals(DsRoot))
				return DsFile.getName();
			else
				return "/";
		}
		
		@Override
		public String getPath() throws RepositoryException
		{
			if(!DsFile.equals(DsRoot))
				return getParent().getPath() + DsFile.getName() + (isDirectory()?"/":"");
			else
				return getName();
		}

		@Override
		public boolean isDirectory() throws RepositoryException
		{
			return DsFile.isDirectory();
		}

		@Override
		public boolean isFile() throws RepositoryException
		{
			return DsFile.isFile();
		}

		@Override
		public long size() throws RepositoryException
		{
			if(isDirectory())
				return -1L;
			else
				return DsFile.length();
		}

		@Override
		public InputStream getInputStream() throws RepositoryException
		{
			if(isDirectory())
				return null;
//			try
//			{
//				return new RemoteInputStream(new RMIInputStreamImpl(new FileInputStream(DsFile)));
//				//return new FileInputStream(DsFile);
//			} catch (FileNotFoundException fnfe) {
//				throw new RepositoryException(fnfe);
//			} catch (RemoteException re) {
//				throw new RepositoryException(re);
//			}
			try {
				return new FileInputStream(DsFile);
			} catch (FileNotFoundException fnfe) {
				throw new RepositoryException(fnfe);
			}
		}

		@Override
		public OutputStream getOutputStream() throws RepositoryException
		{
			if(isDirectory())
				return null;
//			try
//			{
//				return new RemoteOutputStream(new RMIOutputStreamImpl(new FileOutputStream(DsFile)));
//				//return new FileOutputStream(DsFile);
//			} catch (FileNotFoundException fnfe) {
//				throw new RepositoryException(fnfe);
//			} catch (RemoteException re) {
//				throw new RepositoryException(re);
//			}
			try {
				return new FileOutputStream(DsFile);
			} catch (FileNotFoundException fnfe) {
				throw new RepositoryException(fnfe);
			}
		}

		@Override
		public Set<DataFile> children() throws RepositoryException
		{
			Set<DataFile> ds = new TreeSet<DataFile>();
			if(DsFile.listFiles() == null)
				return ds;
			for(File child : DsFile.listFiles())
//				try {
//					ds.add(new RemoteDataFile(new RMIDataFileImpl(new DataSourceImpl(child))));
//				} catch (RemoteException re) {
//					throw new RepositoryException(re);
//				}
				ds.add(new DataSourceImpl(child));
			return ds;
		}
		
		@Override
		public DataFile child(String name) throws RepositoryException
		{
			// Fixed directory traversal exploit
			// File file = new File(DsFile, name);
			if(!new File(DsFile, name).getParentFile().equals(DsFile))
				throw new RepositoryException("Specified file name '" + name + "' is not valid.");
			File file = new File(DsFile, name);
//			try {
//				return new RemoteDataFile(new RMIDataFileImpl(new DataSourceImpl(file)));
//			} catch (RemoteException re) {
//				throw new RepositoryException(re);
//			}
			return new DataSourceImpl(file);
		}

		@Override
		public void touch() throws RepositoryException
		{
			try {
				DsFile.createNewFile();
			} catch (IOException ioe) {
				throw new RepositoryException(ioe);
			}
		}
		
		@Override
		public void mkdir() throws RepositoryException
		{
			if(!DsFile.mkdir())
				if(!DsFile.exists())
					throw new RepositoryException("Could not create directory '" + getName()+ "'.");
		}
		
		@Override
		public void mkdirs() throws RepositoryException
		{
			if(!DsFile.equals(DsRoot))
				getParent().mkdirs();
			mkdir();
		}

		@Override
		public void delete() throws RepositoryException
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
		
		private void _delete(Set<DataFile> dss) throws RepositoryException
		{
			for(DataFile ds : dss)
				if(ds.isDirectory())
				{
					_delete(ds.children());
					ds.delete();
				}
				else
					ds.delete();
		}

		@Override
		public int compareTo(DataFile ds)
		{
			try {
				return getName().compareTo(ds.getName());
			} catch (RepositoryException dse) {
				Core.Logger.log(dse.getMessage(), Level.ERROR);
				dse.printStackTrace();
			}
			return -2;
		}

		@Override
		public boolean exists() throws RepositoryException
		{
			return DsFile.exists();
		}

		@Override
		public DataFile getParent() throws RepositoryException
		{
			if(!DsFile.equals(DsRoot))
//				try {
//					return new RemoteDataFile(new RMIDataFileImpl(new DataSourceImpl(DsFile.getParentFile())));
//				} catch (RemoteException re) {
//					throw new RepositoryException(re);
//				}
				return new DataSourceImpl(DsFile.getParentFile());
			else
//				try {
//					return new RemoteDataFile(new RMIDataFileImpl(new DataSourceImpl(DsFile)));
//				} catch (RemoteException re) {
//					throw new RepositoryException(re);
//				}
				return new DataSourceImpl(DsFile);
		}

		@Override
		public boolean canRead() throws RepositoryException
		{
			return DsFile.canRead();
		}

		@Override
		public boolean canWrite() throws RepositoryException
		{
			return DsFile.canWrite();
		}
	}
}
