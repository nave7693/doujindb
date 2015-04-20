package org.dyndns.doujindb.dat;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.*;

import org.dyndns.doujindb.conf.*;

public final class DataStore
{
	private static IDataStore instance = null;
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(DataStore.class);
	
	public static void open() throws DataStoreException
	{
		LOG.debug("call open()");
		if(isOpen())
			throw new DataStoreException("DataStore is already open.");
		File localPath = Configuration.dat_media_filestore.get();
		if(!localPath.exists())
			throw new DataStoreException("DataStore mountpoint '" + localPath.getPath() + "' does not exists.");
		instance = new LocalDataStore(localPath);
	}

	public static void close() throws DataStoreException
	{
		LOG.debug("call close()");
		if(!isOpen())
			throw new DataStoreException("DataStore is already closed.");
		instance = null;
	}
	
	public static boolean isOpen() throws DataStoreException
	{
		LOG.debug("call isOpen()");
		return instance != null;
	}
	
	static void checkOpen() throws DataStoreException
	{
		if(!isOpen())
			throw new DataStoreException("DataStore is closed.");
	}
	
	public static DataFile.MetaData getMetadata(Integer bookId) throws DataStoreException
	{
		LOG.debug("call getMetadata({})", bookId);
		checkOpen();
		return instance.getMetadata(bookId);
	}
	
	public static DataFile getThumbnail(Integer bookId) throws DataStoreException
	{
		LOG.debug("call getThumbnail({})", bookId);
		checkOpen();
		return instance.getThumbnail(bookId);
	}
	
	public static DataFile getBanner(Integer circleId) throws DataStoreException
	{
		LOG.debug("call getBanner({})", circleId);
		checkOpen();
		return instance.getBanner(circleId);
	}
	
	public static DataFile getStore(Integer bookId) throws DataStoreException
	{
		LOG.debug("call getStore({})", bookId);
		checkOpen();
		return instance.getStore(bookId);
	}
	
	public static void fromFile(File srcPath, DataFile dstPath, CopyOption ... options) throws DataStoreException, IOException
	{
		LOG.debug("call fromFile({}, {}, {})", new Object[]{ srcPath, dstPath, options });
		checkOpen();
		boolean contentsOnly = false;
		boolean overwrite = false;
		for(CopyOption option : options) {
			switch(option) {
			case CONTENTS_ONLY:
				contentsOnly = true;
				break;
			case OVERWRITE:
				overwrite = true;
				break;
			}
		}
		if(contentsOnly)
			for(File file : srcPath.listFiles())
				fromFile(file, dstPath, overwrite);
		else
			fromFile(srcPath, dstPath, overwrite);
	}
	
	/**
	 * Recursively transfer all data contained in srcPath directory into dstPath.
	 * @param srcPath
	 * @param dstPath
	 * @param overwrite
	 * @throws DataStoreException
	 * @throws IOException
	 */
	private static void fromFile(File srcPath, DataFile dstPath, boolean overwrite) throws DataStoreException, IOException
	{
		LOG.debug("call fromFile({}, {})", srcPath, dstPath);
		checkOpen();
		DataFile dataFile = dstPath.getFile(srcPath.getName());
		if(srcPath.isDirectory())
		{
			dataFile.mkdirs();
			for(File file : srcPath.listFiles())
				fromFile(file, dataFile, overwrite);
		} else {
			if(!overwrite && dataFile.exists())
				return;
			Files.copy(srcPath.toPath(), dataFile.openOutputStream());
		}
	}
	
	public static void toFile(DataFile srcPath, File dstPath, CopyOption ... options) throws DataStoreException, IOException
	{
		LOG.debug("call toFile({}, {}, {})", new Object[]{ srcPath, dstPath, options });
		checkOpen();
		boolean contentsOnly = false;
		boolean overwrite = false;
		for(CopyOption option : options) {
			switch(option) {
			case CONTENTS_ONLY:
				contentsOnly = true;
				break;
			case OVERWRITE:
				overwrite = true;
				break;
			}
		}
		if(contentsOnly)
			for(DataFile file : srcPath.listFiles())
				toFile(file, dstPath, overwrite);
		else
			toFile(srcPath, dstPath, overwrite);
	}
	
	/**
	 * Recursively transfer all data contained in srcPath directory into dstPath.
	 * @param srcPath
	 * @param dstPath
	 * @param overwrite
	 * @throws DataStoreException
	 * @throws IOException
	 */
	private static void toFile(DataFile srcPath, File dstPath, boolean overwrite) throws DataStoreException, IOException
	{
		LOG.debug("call toFile({}, {})", srcPath, dstPath);
		checkOpen();
		File file = new File(dstPath, srcPath.getName());
		if(srcPath.isDirectory())
		{
			file.mkdirs();
			for(DataFile dataFile : srcPath.listFiles())
				toFile(dataFile, file, overwrite);
		} else {
			if(!overwrite && file.exists())
				return;
			Files.copy(srcPath.openInputStream(), file.toPath());
		}
	}
	
	/**
	 * Recursively get all files into parentPath.
	 * @param parentPath
	 */
	public static DataFile[] listFiles(DataFile parentPath) throws DataStoreException, IOException
	{
		LOG.debug("call listFiles({})", parentPath);
		checkOpen();
		Set<DataFile> set = new HashSet<DataFile>();
		for(DataFile file : parentPath.listFiles())
			if(file.isDirectory())
				listFiles(file, set);
			else
				set.add(file);
		return set.toArray(new DataFile[] {});
	}
	
	private static DataFile[] listFiles(DataFile parentPath, Set<DataFile> set) throws DataStoreException, IOException
	{
		for(DataFile file : parentPath.listFiles())
			if(file.isDirectory())
				listFiles(file, set);
			else
				set.add(file);
		return set.toArray(new DataFile[] {});
	}
	
	/**
	 * Recursively get all files into parentPath.
	 * @param parentPath
	 */
	public static File[] listFiles(File parentPath) throws DataStoreException, IOException
	{
		LOG.debug("call listFiles({})", parentPath);
		checkOpen();
		Set<File> set = new HashSet<File>();
		for(File file : parentPath.listFiles())
			if(file.isDirectory())
				listFiles(file, set);
			else
				set.add(file);
		return set.toArray(new File[] {});
	}
	
	private static File[] listFiles(File parentPath, Set<File> set) throws DataStoreException, IOException
	{
		for(File file : parentPath.listFiles())
			if(file.isDirectory())
				listFiles(file, set);
			else
				set.add(file);
		return set.toArray(new File[] {});
	}
	
	public static long diskUsage(DataFile path) throws DataStoreException, IOException
	{
		LOG.debug("call diskUsage({})", path);
		checkOpen();
		long du = 0;
		for(DataFile file : listFiles(path))
			if(!file.isDirectory())
				du += file.length();
		return du;
	}
	
	public static long diskUsage(File path) throws DataStoreException, IOException
	{
		LOG.debug("call diskUsage({})", path);
		checkOpen();
		long du = 0;
		Set<File> set = new HashSet<File>();
		for(File file : listFiles(path, set))
			if(!file.isDirectory())
				du += file.length();
		return du;
	}
	
	public static enum CopyOption {
		CONTENTS_ONLY,
		OVERWRITE
	}
}
