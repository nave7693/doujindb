package org.dyndns.doujindb.dat.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import org.dyndns.doujindb.dat.DataSource;

/**  
* RMIDataStore.java - Handle every (meta)data file.
* @author  nozomu
* @version 1.0
*/
public interface RMIDataStore extends Remote
{
	public long size() throws RemoteException;	
	public Set<DataSource> children() throws RemoteException;	
	public DataSource child(String name) throws RemoteException;	
	public DataSource getMetadata(String ID) throws RemoteException;	
	public DataSource getPreview(String ID) throws RemoteException;
}
