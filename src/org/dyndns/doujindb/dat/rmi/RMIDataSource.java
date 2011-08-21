package org.dyndns.doujindb.dat.rmi;

import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import org.dyndns.doujindb.dat.DataSource;

/**  
* RMIDataSource.java
* @author  nozomu
* @version 1.0
*/
public interface RMIDataSource extends Remote
{
	public String getName() throws RemoteException;	
	public String getPath() throws RemoteException;	
	public boolean isDirectory() throws RemoteException;	
	public boolean isFile() throws RemoteException;	
	public boolean canRead() throws RemoteException;	
	public boolean canWrite() throws RemoteException;	
	public long size() throws RemoteException;	
	public InputStream getInputStream() throws RemoteException;	
	public OutputStream getOutputStream() throws RemoteException;	
	public Set<DataSource> children() throws RemoteException;	
	public DataSource getParent() throws RemoteException;	
	public void touch() throws RemoteException;	
	public void mkdir() throws RemoteException;	
	public void mkdirs() throws RemoteException;	
	public DataSource child(String name) throws RemoteException;	
	public boolean exists() throws RemoteException;	
	public void delete() throws RemoteException;
	public int compareTo(DataSource ds) throws RemoteException;
}
