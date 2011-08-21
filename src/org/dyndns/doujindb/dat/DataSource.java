package org.dyndns.doujindb.dat;

import java.io.*;
import java.util.Set;

/**  
* DataSource.java
* @author  nozomu
* @version 1.0
*/
public interface DataSource
{
	public String getName() throws DataStoreException;	
	public String getPath() throws DataStoreException;	
	public boolean isDirectory() throws DataStoreException;	
	public boolean isFile() throws DataStoreException;	
	public boolean canRead() throws DataStoreException;	
	public boolean canWrite() throws DataStoreException;	
	public long size() throws DataStoreException;	
	public InputStream getInputStream() throws DataStoreException;	
	public OutputStream getOutputStream() throws DataStoreException;	
	public Set<DataSource> children() throws DataStoreException;	
	public DataSource getParent() throws DataStoreException;	
	public void touch() throws DataStoreException;	
	public void mkdir() throws DataStoreException;	
	public void mkdirs() throws DataStoreException;	
	public DataSource child(String name) throws DataStoreException;	
	public boolean exists() throws DataStoreException;	
	public void delete() throws DataStoreException;
	public int compareTo(DataSource ds) throws DataStoreException;
}
