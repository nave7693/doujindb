package org.dyndns.doujindb.dat.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import org.dyndns.doujindb.dat.*;

/**  
* RMIDataFileImpl.java
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class RMIDataFileImpl extends UnicastRemoteObject implements RMIDataFile
{
	private DataFile ds;

	public RMIDataFileImpl(DataFile ds) throws RemoteException
	{
		super(1099);
		this.ds = ds;
	}
	
	public String getName() throws RemoteException {
		try {
			return ds.getName();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public String getPath() throws RemoteException {
		try {
			return ds.getPath();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public boolean isDirectory() throws RemoteException {
		try {
			return ds.isDirectory();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public boolean isFile() throws RemoteException {
		try {
			return ds.isFile();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}	
	public boolean canRead() throws RemoteException {
		try {
			return ds.canRead();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public boolean canWrite() throws RemoteException {
		try {
			return ds.canWrite();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public long size() throws RemoteException {
		try {
			return ds.size();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public InputStream getInputStream() throws RemoteException {
		try {
			return ds.getInputStream();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public OutputStream getOutputStream() throws RemoteException {
		try {
			return ds.getOutputStream();
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

	public DataFile getParent() throws RemoteException {
		try {
			return ds.getParent();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public void touch() throws RemoteException {
		try {
			ds.touch();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public void mkdir() throws RemoteException {
		try {
			ds.mkdir();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public void mkdirs() throws RemoteException {
		try {
			ds.mkdirs();
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

	public boolean exists() throws RemoteException {
		try {
			return ds.exists();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	public void delete() throws RemoteException {
		try {
			ds.delete();
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}

	@Override
	public int compareTo(DataFile ds) throws RemoteException
	{
		try {
			return this.ds.compareTo(ds);
		} catch (RepositoryException dse) {
			throw new RemoteException("Repository Exception on the server", dse);
		}
	}
}
