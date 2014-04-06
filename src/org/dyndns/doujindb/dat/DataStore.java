package org.dyndns.doujindb.dat;

import java.io.*;
import java.nio.file.*;

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
	 * @param bookId
	 * @param basePath
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
}
