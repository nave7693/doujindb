package org.dyndns.doujindb.dat;

import java.util.Set;

/**  
* DataStore.java - Handle (almost) every I/O operation on disk
* @author  nozomu
* @version 1.0
*/
public interface DataStore
{
	/*public boolean isConnected() throws DataStoreException;
	
	public void connect(String connectionString) throws DataStoreException;
	
	public void disconnect() throws DataStoreException;*/
	
	public long size() throws DataStoreException;
	
	public Set<DataSource> children() throws DataStoreException;
	
	public DataSource child(String name) throws DataStoreException;
	
	public DataSource getMetadata(String ID) throws DataStoreException;
	
	public DataSource getPreview(String ID) throws DataStoreException;
}
