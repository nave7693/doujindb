package org.dyndns.doujindb.dat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.conf.*;

final class LocalDataStore implements IDataStore
{
	private final File rootPath;
	
	private static final String DATAFILE_ROOTFS = "local://";
	private static final String DATAFILE_META = ".xml";
	private static final String DATAFILE_THUMBNAIL = ".thumb";
	private static final String DATAFILE_BANNER = ".thumb";
	
	@SuppressWarnings("unused")
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(LocalDataStore.class);
	
	LocalDataStore(final File rootPath)
	{
		this.rootPath = rootPath;
	}
	
	private File mapIdStore(Integer recordId, String prefix)
	{
		/**
		 * Root-Path
		 *  - 00
		 *     - B00000000
		 *     - B00000100
		 *     - B00000200
		 *  - 01
		 *     - B00000001
		 *  ...
		 *  - ff
		 *     - B000000ff
		 */
		return new File(new File(rootPath, String.format("%02x", (recordId % 256))), String.format("%s%08x", prefix, recordId));
	}
	
	@Override
	public DataFile.MetaData getMetadata(Integer bookId) throws DataStoreException
	{
		DataStore.checkOpen();
		
		return new DataFile.MetaData(){};
	}

	@Override
	public DataFile getThumbnail(Integer bookId) throws DataStoreException
	{
		DataStore.checkOpen();
		
		File store = mapIdStore(bookId, "B");
		store.mkdirs();
		
		return new LocalDataFile(new File(store, DATAFILE_THUMBNAIL), store.getName());
	}
	
	@Override
	public DataFile getBanner(Integer circleId) throws DataStoreException
	{
		DataStore.checkOpen();
		
		File store = mapIdStore(circleId, "C");
		store.mkdirs();
		
		return new LocalDataFile(new File(store, DATAFILE_BANNER), store.getName());	}

	@Override
	public DataFile getStore(Integer bookId) throws DataStoreException
	{
		DataStore.checkOpen();
		
		File store = mapIdStore(bookId, "B");
		store.mkdirs();
		
		return new LocalDataFile(store);
	}
	
	private final class LocalDataFile implements DataFile, Comparable<DataFile>
	{
		private final File filePath;
		private final String cacheId;
		
		public LocalDataFile(final File filePath)
		{
			this(filePath, null);
		}
		
		public LocalDataFile(final File filePath, final String cacheId)
		{
			this.filePath = filePath;
			this.cacheId = cacheId;
		}
		
		private boolean isCacheable()
		{
			return cacheId != null;
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
		public InputStream openInputStream() throws DataStoreException
		{
			DataStore.checkOpen();
			
			if(LocalCache.isEnabled() && this.isCacheable())
				try {
					File file = LocalCache.get(cacheId);
					return new FileInputStream(file);
				} catch (FileNotFoundException fnfe) {
					try {
						FileInputStream stream = new FileInputStream(filePath);
						LocalCache.put(cacheId, filePath);
						return stream;
					} catch (FileNotFoundException fnfe2) {
						throw new DataStoreException(fnfe2);
					}
				}
			else
				try {
					return new FileInputStream(filePath);
				} catch (FileNotFoundException fnfe) {
					throw new DataStoreException(fnfe);
				}
		}

		@Override
		public OutputStream openOutputStream() throws DataStoreException
		{
			DataStore.checkOpen();
			
			if(LocalCache.isEnabled() && this.isCacheable())
				try {
					return new FileOutputStream(filePath) {
						@Override
						public void close() throws IOException {
							super.close();
							LocalCache.put(cacheId, filePath);
						}
					};
				} catch (FileNotFoundException fnfe) {
					throw new DataStoreException(fnfe);
				}
			else
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
					if(name.equals(DATAFILE_THUMBNAIL))
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
				if(recursive)
					deleteRecursive(listFiles());
				if(!filePath.delete())
					filePath.deleteOnExit();
			}
			else	
				if(!filePath.delete())
					filePath.deleteOnExit();
			
			if(LocalCache.isEnabled() && this.isCacheable())
				LocalCache.purge(cacheId);
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

		@Override
		public long lastModified() throws DataStoreException {
			return filePath.lastModified();
		}
	}
	
	private static final class LocalCache
	{
		private static File cachePath = Configuration.dat_cache_filestore.get();
		private static boolean cacheEnabled = Configuration.dat_cache_enable.get();
		
		private static ConcurrentLinkedQueue<CachePair<File>> queue = new ConcurrentLinkedQueue<CachePair<File>>();
		
		private static final Logger LOG = (Logger) LoggerFactory.getLogger(LocalDataStore.class);
		
		private static final class CachePair<T>
		{
			private final String id;
			private final T object;
			
			private CachePair(String id, T object)
			{
				this.id = id;
				this.object = object;
			}
		}
		
		static
		{
			new Thread() {
				@Override
				public void run() {
					super.setName("datastore-cacheupdater");
					while(true) {
						try {
							Thread.sleep(1);
							if(queue.isEmpty())
								continue;
							update(queue.poll());
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Error e) {
							e.printStackTrace();
						}
					}
				}
				private void update(CachePair<File> pair) {
					try {
						Files.copy(pair.object.toPath(), new File(cachePath, pair.id).toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException ioe) {
						LOG.error("Error putting [{}] in local cache", pair.id, ioe);
					}
				}
			}.start();
		}
		
		public static boolean isEnabled()
		{
			return cacheEnabled;
		}
		
		public static File get(String cacheId) throws FileNotFoundException
		{
			LOG.debug("call get({})", cacheId);
			File cached =  new File(cachePath, cacheId);
			if(!cached.exists())
				throw new FileNotFoundException();
			return cached;
		}
		
		public static void put(String cacheId, File file)
		{
			LOG.debug("call put({}, {})", cacheId, file);
			queue.offer(new CachePair<File>(cacheId, file));
		}
		
		public static void purge(String cacheId)
		{
			LOG.debug("call purge({})", cacheId);
			File cached = new File(cachePath, cacheId);
			if(!cached.delete())
				cached.deleteOnExit();
		}
	}
}
