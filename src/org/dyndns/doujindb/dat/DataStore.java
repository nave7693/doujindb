package org.dyndns.doujindb.dat;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.dyndns.doujindb.conf.*;
import org.dyndns.doujindb.log.*;

public final class DataStore
{
	private static IDataStore instance = null;
	
	private static final String TAG = "DataStore : ";
	
	static
	{
		Logger.logInfo(TAG + "initializing.");
	}
	
	public static void open() throws DataStoreException
	{
		if(instance != null)
			throw new DataStoreException("DataStore is already open.");
		File localPath = new File(Configuration.configRead("org.dyndns.doujindb.dat.datastore").toString());
		if(!localPath.exists())
			throw new DataStoreException("DataStore mountpoint '" + localPath.getPath() + "' does not exists.");
		instance = new LocalDataStore(localPath);
	}

	public static void close() throws DataStoreException
	{
		if(instance == null)
			throw new DataStoreException("DataStore is already closed.");
		instance = null;
	}
	
	public static boolean isOpen() throws DataStoreException
	{
		return instance != null;
	}
	
	public static DataFile getMeta(String bookId) throws DataStoreException
	{
		if(instance == null)
			throw new DataStoreException("DataStore is closed.");
		return instance.getMeta(bookId);
	}
	
	public static DataFile getCover(String bookId) throws DataStoreException
	{
		if(instance == null)
			throw new DataStoreException("DataStore is closed.");
		return instance.getCover(bookId);
	}
	
	public static DataFile getFile(String bookId) throws DataStoreException
	{
		if(instance == null)
			throw new DataStoreException("DataStore is closed.");
		return instance.getFile(bookId);
	}
	
	public static void fromFile(File srcPath, DataFile dstPath, boolean contentsOnly) throws DataStoreException, IOException
	{
		if(contentsOnly)
			for(File file : srcPath.listFiles())
				fromFile(file, dstPath);
		else
			fromFile(srcPath, dstPath);
	}
	
	/**
	 * Recursively transfer all data contained in srcPath directory into dstPath.
	 * @param srcPath
	 * @param dstPath
	 * @throws DataStoreException
	 * @throws IOException
	 */
	public static void fromFile(File srcPath, DataFile dstPath) throws DataStoreException, IOException
	{
		DataFile dataFile = dstPath.getFile(srcPath.getName());
		if(srcPath.isDirectory())
		{
			dataFile.mkdirs();
			for(File file : srcPath.listFiles())
				fromFile(file, dataFile);
		} else {
			Files.copy(srcPath.toPath(), dataFile.getOutputStream());
		}
	}
	
	public static void toFile(DataFile srcPath, File dstPath, boolean contentsOnly) throws DataStoreException, IOException
	{
		if(contentsOnly)
			for(DataFile file : srcPath.listFiles())
				toFile(file, dstPath);
		else
			toFile(srcPath, dstPath);
	}
	
	/**
	 * Recursively transfer all data contained in srcPath directory into dstPath.
	 * @param srcPath
	 * @param dstPath
	 * @throws DataStoreException
	 * @throws IOException
	 */
	public static void toFile(DataFile srcPath, File dstPath) throws DataStoreException, IOException
	{
		File file = new File(dstPath, srcPath.getName());
		if(srcPath.isDirectory())
		{
			file.mkdirs();
			for(DataFile dataFile : srcPath.listFiles())
				toFile(dataFile, file);
		} else {
			Files.copy(srcPath.getInputStream(), file.toPath());
		}
	}
	
	/**
	 * Recursively get all files into parentPath.
	 * @param parentPath
	 */
	public static DataFile[] listFiles(DataFile parentPath) throws DataStoreException, IOException
	{
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
		long du = 0;
		for(DataFile file : listFiles(path))
			if(!file.isDirectory())
				du += file.length();
		return du;
	}
	
	public static long diskUsage(File path) throws DataStoreException, IOException
	{
		long du = 0;
		Set<File> set = new HashSet<File>();
		for(File file : listFiles(path, set))
			if(!file.isDirectory())
				du += file.length();
		return du;
	}
}
