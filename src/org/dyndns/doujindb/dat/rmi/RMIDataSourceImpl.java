package org.dyndns.doujindb.dat.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import org.dyndns.doujindb.dat.*;

/**  
* RMIDataSourceImpl.java
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class RMIDataSourceImpl extends UnicastRemoteObject implements RMIDataSource
{
	private DataSource ds;

	public RMIDataSourceImpl(DataSource ds) throws RemoteException
	{
		super(1099);
		this.ds = ds;
	}
	
	public String getName() throws RemoteException {
		try {
			return ds.getName();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public String getPath() throws RemoteException {
		try {
			return ds.getPath();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public boolean isDirectory() throws RemoteException {
		try {
			return ds.isDirectory();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public boolean isFile() throws RemoteException {
		try {
			return ds.isFile();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}	
	public boolean canRead() throws RemoteException {
		try {
			return ds.canRead();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public boolean canWrite() throws RemoteException {
		try {
			return ds.canWrite();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public long size() throws RemoteException {
		try {
			return ds.size();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public InputStream getInputStream() throws RemoteException {
		try {
			return ds.getInputStream();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public OutputStream getOutputStream() throws RemoteException {
		try {
			return ds.getOutputStream();
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

	public DataSource getParent() throws RemoteException {
		try {
			return ds.getParent();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public void touch() throws RemoteException {
		try {
			ds.touch();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public void mkdir() throws RemoteException {
		try {
			ds.mkdir();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public void mkdirs() throws RemoteException {
		try {
			ds.mkdirs();
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

	public boolean exists() throws RemoteException {
		try {
			return ds.exists();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	public void delete() throws RemoteException {
		try {
			ds.delete();
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}

	@Override
	public int compareTo(DataSource ds) throws RemoteException
	{
		try {
			return this.ds.compareTo(ds);
		} catch (DataStoreException dse) {
			throw new RemoteException("DataStore Exception on the server", dse);
		}
	}
}
