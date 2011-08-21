package org.dyndns.doujindb.dat;

import java.util.Set;

/**  
* DataStore.java - Handle every (meta)data file.
* @author  nozomu
* @version 1.0
*/
public interface DataStore
{
	public long size() throws DataStoreException;	
	public Set<DataSource> children() throws DataStoreException;	
	public DataSource child(String name) throws DataStoreException;	
	public DataSource getMetadata(String ID) throws DataStoreException;	
	public DataSource getPreview(String ID) throws DataStoreException;
}
