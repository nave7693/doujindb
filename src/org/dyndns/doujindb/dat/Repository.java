package org.dyndns.doujindb.dat;

import java.util.Set;

/**  
* Repository.java - Handle every (meta)data file.
* @author  nozomu
* @version 1.0
*/
public interface Repository
{
	public long size() throws RepositoryException;	
	public Set<DataFile> children() throws RepositoryException;	
	public DataFile child(String name) throws RepositoryException;	
	public DataFile getMetadata(String ID) throws RepositoryException;	
	public DataFile getPreview(String ID) throws RepositoryException;
}
