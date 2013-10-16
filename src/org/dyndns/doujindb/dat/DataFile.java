package org.dyndns.doujindb.dat;

import java.io.*;
import java.util.Set;

/**  
* DataFile.java
* @author  nozomu
* @version 1.0
*/
public interface DataFile
{
	public String getName() throws RepositoryException;	
	public String getPath() throws RepositoryException;	
	public boolean isDirectory() throws RepositoryException;	
	public boolean isFile() throws RepositoryException;	
	public boolean canRead() throws RepositoryException;	
	public boolean canWrite() throws RepositoryException;	
	public long size() throws RepositoryException;	
	public InputStream getInputStream() throws RepositoryException;	
	public OutputStream getOutputStream() throws RepositoryException;	
	public Set<DataFile> children() throws RepositoryException;	
	public DataFile getParent() throws RepositoryException;	
	public void touch() throws RepositoryException;	
	public void mkdir() throws RepositoryException;	
	public void mkdirs() throws RepositoryException;	
	public DataFile child(String name) throws RepositoryException;	
	public boolean exists() throws RepositoryException;	
	public void delete() throws RepositoryException;
	public int compareTo(DataFile ds) throws RepositoryException;
}
