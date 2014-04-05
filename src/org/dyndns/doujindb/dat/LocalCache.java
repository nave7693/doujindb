package org.dyndns.doujindb.dat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.dyndns.doujindb.conf.Configuration;

final class LocalCache
{
	private static File cachePath = new File((String) Configuration.configRead("org.dyndns.doujindb.dat.cache_dir"));
	private static boolean cacheEnabled = (Boolean) Configuration.configRead("org.dyndns.doujindb.dat.keep_cache");
	
	@SuppressWarnings("unused")
	private static final String TAG = "LocalCache : ";
	
	public static boolean isEnabled()
	{
		return cacheEnabled;
	}
	
	public static File get(String id) throws IOException
	{
		return new File(cachePath, id);
	}
	
	public static void put(String id, File file) throws IOException
	{
		Files.copy(file.toPath(), new File(cachePath, id).toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
}
