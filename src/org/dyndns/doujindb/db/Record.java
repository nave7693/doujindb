package org.dyndns.doujindb.db;

import java.io.Serializable;
import java.rmi.*;

/**  
* Record.java - DoujinDB database record.
* @author  nozomu
* @version 1.0
*/
public interface Record extends Remote, Serializable
{
	public String getID() throws RemoteException;
	void setID(long id) throws RemoteException;
}