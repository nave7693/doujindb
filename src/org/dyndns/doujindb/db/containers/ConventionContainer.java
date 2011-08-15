package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.records.Convention;

/**  
* ConventionContainer.java - Interface every item in the DB containing convention(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface ConventionContainer extends Remote
{
	public Convention getConvention() throws RemoteException;
	public void setConvention(Convention convention) throws RemoteException;
}
