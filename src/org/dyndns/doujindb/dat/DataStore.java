package org.dyndns.doujindb.dat;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**  
* DataStore.java - Handle every (meta)data file.
* @author  nozomu
* @version 1.0
*/
public interface DataStore extends Remote, Serializable
{
	public long size() throws DataStoreException, RemoteException;
	
	public Set<DataSource> children() throws DataStoreException, RemoteException;
	
	public DataSource child(String name) throws DataStoreException, RemoteException;
	
	public DataSource getMetadata(String ID) throws DataStoreException, RemoteException;
	
	public DataSource getPreview(String ID) throws DataStoreException, RemoteException;
}
