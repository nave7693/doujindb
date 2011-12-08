package org.dyndns.doujindb.dat.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import org.dyndns.doujindb.dat.DataFile;

/**  
* RMIRepository.java - Handle every (meta)data file.
* @author  nozomu
* @version 1.0
*/
public interface RMIRepository extends Remote
{
	public long size() throws RemoteException;	
	public Set<DataFile> children() throws RemoteException;	
	public DataFile child(String name) throws RemoteException;	
	public DataFile getMetadata(String ID) throws RemoteException;	
	public DataFile getPreview(String ID) throws RemoteException;
}
