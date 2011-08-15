package org.dyndns.doujindb.db;

import java.io.Serializable;
import java.rmi.*;

/**  
* Table.java - DoujinDB database table.
* @author  nozomu
* @version 1.0
*/
public interface Table<T extends Record> extends Remote, Serializable
{
	public void insert(T row) throws DataBaseException, RemoteException;
	public void delete(T row) throws DataBaseException, RemoteException;
	public boolean contains(T row) throws DataBaseException, RemoteException;
	public long count() throws DataBaseException, RemoteException;
	public Iterable<T> elements() throws DataBaseException, RemoteException;
}