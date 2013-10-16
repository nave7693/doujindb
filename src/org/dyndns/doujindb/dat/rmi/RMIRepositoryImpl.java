package org.dyndns.doujindb.dat.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import org.dyndns.doujindb.dat.*;

/**  
* RMIRepositoryImpl.java - Handle every (meta)data file.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class RMIRepositoryImpl extends UnicastRemoteObject implements RMIRepository
{
	private Repository ds;

	public RMIRepositoryImpl(Repository ds) throws RemoteException
	{
		super(1099);
		this.ds = ds;
	}
	
	public long size() throws RemoteException {
		try {
			return ds.size();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public Set<DataFile> children() throws RemoteException {
		try {
			return ds.children();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public DataFile child(String name) throws RemoteException {
		try {
			return ds.child(name);
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public DataFile getMetadata(String ID) throws RemoteException {
		try {
			return ds.getMetadata(ID);
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public DataFile getPreview(String ID) throws RemoteException {
		try {
			return ds.getPreview(ID);
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}
}
