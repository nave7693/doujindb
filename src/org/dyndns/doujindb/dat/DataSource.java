package org.dyndns.doujindb.dat;

import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**  
* DataSource.java
* @author  nozomu
* @version 1.0
*/
public interface DataSource extends Remote, Serializable
{
	public String getName() throws DataStoreException, RemoteException;
	
	public String getPath() throws DataStoreException, RemoteException;
	
	public boolean isDirectory() throws DataStoreException, RemoteException;
	
	public boolean isFile() throws DataStoreException, RemoteException;
	
	public boolean canRead() throws DataStoreException, RemoteException;
	
	public boolean canWrite() throws DataStoreException, RemoteException;
	
	public long size() throws DataStoreException, RemoteException;
	
	public InputStream getInputStream() throws DataStoreException, RemoteException;
	
	public OutputStream getOutputStream() throws DataStoreException, RemoteException;
	
	public Set<DataSource> children() throws DataStoreException, RemoteException;
	
	public DataSource getParent() throws DataStoreException, RemoteException;
	
	public void touch() throws DataStoreException, RemoteException;
	
	public void mkdir() throws DataStoreException, RemoteException;
	
	public void mkdirs() throws DataStoreException, RemoteException;
	
	public DataSource child(String name) throws DataStoreException, RemoteException;
	
	public boolean exists() throws DataStoreException, RemoteException;
	
	public void delete() throws DataStoreException, RemoteException;
}
