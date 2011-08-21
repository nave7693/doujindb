package org.dyndns.doujindb.dat.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import org.dyndns.doujindb.dat.*;

/**  
* RMIDataStoreImpl.java - Handle every (meta)data file.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class RMIDataStoreImpl extends UnicastRemoteObject implements RMIDataStore
{
	private DataStore ds;

	public RMIDataStoreImpl(DataStore ds) throws RemoteException
	{
		this.ds = ds;
	}
	
	public long size() throws RemoteException {
		try {
			return ds.size();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public Set<DataSource> children() throws RemoteException {
		try {
			return ds.children();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public DataSource child(String name) throws RemoteException {
		try {
			return ds.child(name);
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public DataSource getMetadata(String ID) throws RemoteException {
		try {
			return ds.getMetadata(ID);
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public DataSource getPreview(String ID) throws RemoteException {
		try {
			return ds.getPreview(ID);
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}
}
