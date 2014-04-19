package org.dyndns.doujindb.dat;

import java.io.*;

import org.dyndns.doujindb.log.Logger;

final class LocalDataStore implements IDataStore
{
	private final File rootPath;
	
	private static final String DATAFILE_ROOTFS = "local://";
	private static final String DATAFILE_META = ".xml";
	private static final String DATAFILE_COVER = ".preview";
	
	private static final String TAG = "LocalDataStore : ";
	
	LocalDataStore(final File rootPath)
	{
		this.rootPath = rootPath;
	}
	
	@Override
	public DataFile getMeta(String bookId) throws DataStoreException
	{
		DataStore.checkOpen();
		
		return new LocalDataFile(new File(new File(rootPath, bookId), DATAFILE_META));
	}

	@Override
	public DataFile getCover(String bookId) throws DataStoreException
	{
		DataStore.checkOpen();
		
		if(LocalCache.isEnabled())
			try {
				File file = LocalCache.get(bookId);
				if(file.exists())
					return new LocalDataFile(file);
				else
					throw new IOException();
			} catch (IOException ioe) {
				File file = new File(new File(rootPath, bookId), DATAFILE_COVER);
				try {
					LocalCache.put(bookId, file);
				} catch (Exception e) {
					Logger.logError(TAG + "failed to add '" + bookId + "' to local cache.", e);
				}
				return new LocalDataFile(file);
			}
		else
			return new LocalDataFile(new File(new File(rootPath, bookId), DATAFILE_COVER));
	}

	@Override
	public DataFile getFile(String bookId) throws DataStoreException
	{
		DataStore.checkOpen();
		
		return new LocalDataFile(new File(rootPath, bookId));
	}
	
	private final class LocalDataFile implements DataFile, Comparable<DataFile>
	{
		private final File filePath;
		
		public LocalDataFile(final File filePath)
		{
			this.filePath = filePath;
		}

		@Override
		public String getName() throws DataStoreException
		{
			DataStore.checkOpen();
			
			return filePath.getName();
		}

		@Override
		public String getPath() throws DataStoreException
		{
			DataStore.checkOpen();
			
			if(!filePath.equals(rootPath))
				return getParent().getPath() + File.separator + filePath.getName();
			else
				return DATAFILE_ROOTFS + getName();
		}

		@Override
		public boolean isDirectory() throws DataStoreException
		{
			DataStore.checkOpen();
			
			return filePath.isDirectory();
		}

		@Override
		public boolean isFile() throws DataStoreException
		{
			DataStore.checkOpen();
			
			return filePath.isFile();
		}

		@Override
		public boolean canRead() throws DataStoreException
		{
			DataStore.checkOpen();
			
			return filePath.canRead();
		}

		@Override
		public boolean canWrite() throws DataStoreException
		{
			DataStore.checkOpen();
			
			return filePath.canWrite();
		}

		@Override
		public String size() throws DataStoreException
		{
			DataStore.checkOpen();
			
			long bytes = length();
			int unit = 1024;
		    if (bytes < unit) return bytes + " B";
		    int exp = (int) (Math.log(bytes) / Math.log(unit));
		    String pre = ("KMGTPE").charAt(exp-1) + ("i");
		    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}
		
		@Override
		public long length() throws DataStoreException
		{
			DataStore.checkOpen();
			
			return filePath.length();
		}

		@Override
		public InputStream getInputStream() throws DataStoreException
		{
			DataStore.checkOpen();
			
			try {
				return new FileInputStream(filePath);
			} catch (FileNotFoundException fnfe) {
				throw new DataStoreException(fnfe);
			}
		}

		@Override
		public OutputStream getOutputStream() throws DataStoreException
		{
			DataStore.checkOpen();
			
			try {
				return new FileOutputStream(filePath);
			} catch (FileNotFoundException fnfe) {
				throw new DataStoreException(fnfe);
			}
		}
		
		@Override
		public DataFile getFile(String name) throws DataStoreException
		{
			DataStore.checkOpen();
			
			File newFile = new File(filePath, name);
			/**
			 * Try to prevent possible directory traversal attack
			 * DataFile.getFile("../../../../../../../../../etc/passwd")
			 * @see http://en.wikipedia.org/wiki/Directory_traversal_attack
			 */
			if(!newFile.getParentFile().equals(filePath))
				throw new DataStoreException("Specified file name '" + name + "' is not valid.");
			return new LocalDataFile(newFile);
		}
		
		@Override
		public DataFile[] listFiles() throws DataStoreException
		{
			DataStore.checkOpen();
			
			return listFiles(".*");
		}

		@Override
		public DataFile[] listFiles(final String regexp) throws DataStoreException
		{
			DataStore.checkOpen();
			
			if(!filePath.exists())
				return new DataFile[]{};
			java.util.Set<DataFile> files = new java.util.TreeSet<DataFile>();
			for(File file : filePath.listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name) {
					if(!filePath.equals(dir))
						return true;
					if(name.equals(DATAFILE_META))
						return false;
					if(name.equals(DATAFILE_COVER))
						return false;
					if(name.matches(regexp))
						return true;
					return true;
				}
			}))
			{
				files.add(new LocalDataFile(file));
			}
			return files.toArray(new DataFile[]{});
		}

		@Override
		public void touch() throws DataStoreException
		{
			DataStore.checkOpen();
			
			try {
				filePath.getParentFile().mkdirs();
				filePath.createNewFile();
			} catch (IOException ioe) {
				throw new DataStoreException(ioe);
			}
		}

		@Override
		public void mkdir() throws DataStoreException
		{
			DataStore.checkOpen();
			
			filePath.mkdir();
		}
		
		@Override
		public void mkdirs() throws DataStoreException
		{
			DataStore.checkOpen();
			
			filePath.mkdirs();
		}

		@Override
		public boolean exists() throws DataStoreException
		{
			DataStore.checkOpen();
			
			return filePath.exists();
		}

		@Override
		public void delete() throws DataStoreException
		{
			DataStore.checkOpen();
			
			delete(false);
		}
		
		@Override
		public void delete(boolean recursive) throws DataStoreException
		{
			DataStore.checkOpen();
			
			if(filePath.equals(rootPath))
				return;
			if(isDirectory())
			{
				if(!recursive)
					return;
				deleteRecursive(listFiles());
				if(!filePath.delete())
					filePath.deleteOnExit();
			}
			else	
				if(!filePath.delete())
					filePath.deleteOnExit();
		}
		
		private void deleteRecursive(DataFile[] files) throws DataStoreException
		{
			for(DataFile file : files)
				if(file.isDirectory())
				{
					deleteRecursive(file.listFiles());
					file.delete();
				}
				else
					file.delete();
		}

		@Override
		public int compareTo(DataFile dataFile) {
			return filePath.compareTo(((LocalDataFile)dataFile).filePath);
		}
		
		@Override
		public String toString() {
			try {
				return getPath();
			} catch (DataStoreException dse) {
				dse.printStackTrace();
			}
			return super.toString();
		}
		
		private DataFile getParent()
		{
			if(!filePath.equals(rootPath))
				return new LocalDataFile(filePath.getParentFile());
			else
				return new LocalDataFile(filePath);
		}

		@Override
		public void browse() throws DataStoreException
		{
			DataStore.checkOpen();
			
			try {
				java.awt.Desktop.getDesktop().browse(filePath.toURI());
			} catch (IOException ioe) {
				throw new DataStoreException(ioe);
			}
		}
	}
}
