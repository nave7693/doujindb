package org.dyndns.doujindb.dat;

import java.io.*;

final class LocalDataStore implements IDataStore
{
	private final File rootPath;
	
	private static final String DATAFILE_ROOTFS = "local://";
	private static final String DATAFILE_META = ".xml";
	private static final String DATAFILE_COVER = ".preview";
	
	@SuppressWarnings("unused")
	private static final String TAG = "LocalDataStore : ";
	
	LocalDataStore(final File rootPath)
	{
		this.rootPath = rootPath;
	}
	
	@Override
	public DataFile getMeta(String bookId) throws DataStoreException
	{
		return new LocalDataFile(new File(new File(rootPath, bookId), DATAFILE_META));
	}

	@Override
	public DataFile getCover(String bookId) throws DataStoreException
	{
		return new LocalDataFile(new File(new File(rootPath, bookId), DATAFILE_COVER));
	}

	@Override
	public DataFile getFile(String bookId) throws DataStoreException
	{
		return new LocalDataFile(new File(rootPath, bookId));
	}
	
	private final class LocalDataFile implements DataFile
	{
		private final File filePath;
		
		public LocalDataFile(final File filePath)
		{
			this.filePath = filePath;
		}

		@Override
		public String getName() throws DataStoreException {
			return filePath.getName();
		}

		@Override
		public String getPath() throws DataStoreException {
			if(!filePath.equals(rootPath))
				return DATAFILE_ROOTFS + getParent().getPath() + filePath.getName() + (isDirectory() ? "/" : "");
			else
				return getName();
		}

		@Override
		public boolean isDirectory() throws DataStoreException {
			return filePath.isDirectory();
		}

		@Override
		public boolean isFile() throws DataStoreException {
			return filePath.isFile();
		}

		@Override
		public boolean canRead() throws DataStoreException {
			return filePath.canRead();
		}

		@Override
		public boolean canWrite() throws DataStoreException {
			return filePath.canWrite();
		}

		@Override
		public String size() throws DataStoreException {
			long bytes = length();
			int unit = 1024;
		    if (bytes < unit) return bytes + " B";
		    int exp = (int) (Math.log(bytes) / Math.log(unit));
		    String pre = ("KMGTPE").charAt(exp-1) + ("i");
		    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}
		
		@Override
		public long length() throws DataStoreException {
			return filePath.length();
		}

		@Override
		public InputStream getInputStream() throws DataStoreException {
			try {
				return new FileInputStream(filePath);
			} catch (FileNotFoundException fnfe) {
				throw new DataStoreException(fnfe);
			}
		}

		@Override
		public OutputStream getOutputStream() throws DataStoreException {
			try {
				return new FileOutputStream(filePath);
			} catch (FileNotFoundException fnfe) {
				throw new DataStoreException(fnfe);
			}
		}
		
		@Override
		public DataFile[] listFiles() throws DataStoreException {
			return listFiles(".*");
		}

		@Override
		public DataFile[] listFiles(final String regexp) throws DataStoreException {
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
		public void touch() throws DataStoreException {
			try {
				filePath.getParentFile().mkdirs();
				filePath.createNewFile();
			} catch (IOException ioe) {
				throw new DataStoreException(ioe);
			}
		}

		@Override
		public void mkdir() throws DataStoreException {
			filePath.mkdir();
		}
		
		@Override
		public void mkdirs() throws DataStoreException {
			filePath.mkdirs();
		}

		@Override
		public boolean exists() throws DataStoreException {
			return filePath.exists();
		}

		@Override
		public void delete() throws DataStoreException {
			filePath.delete();
		}

		@Override
		public int compareTo(DataFile dataFile) throws DataStoreException {
			return filePath.compareTo(((LocalDataFile)dataFile).filePath);
		}
		
		private DataFile getParent()
		{
			if(!filePath.equals(rootPath))
				return new LocalDataFile(filePath.getParentFile());
			else
				return new LocalDataFile(filePath);
		}
	}
}
