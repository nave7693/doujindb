package org.dyndns.doujindb.dat;

import java.io.*;

interface DataFile
{
	public String getName() throws DataStoreException;
	public String getPath() throws DataStoreException;
	public boolean isDirectory() throws DataStoreException;	
	public boolean isFile() throws DataStoreException;
	public boolean canRead() throws DataStoreException;
	public boolean canWrite() throws DataStoreException;	
	public long length() throws DataStoreException;
	public String size() throws DataStoreException;
	public InputStream getInputStream() throws DataStoreException;
	public OutputStream getOutputStream() throws DataStoreException;	
	public DataFile[] listFiles() throws DataStoreException;	
	public void touch() throws DataStoreException;
	public void mkdir() throws DataStoreException;
	public void mkdirs() throws DataStoreException;
	public boolean exists() throws DataStoreException;
	public void delete() throws DataStoreException;
	public int compareTo(DataFile dataFile) throws DataStoreException;
}
