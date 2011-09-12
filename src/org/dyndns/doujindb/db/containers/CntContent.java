package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.*;

/**  
* CntContent.java - Interface every item in the DB containing content(s) [ok, i lol'd] must implement.
* @author nozomu
* @version 1.0
*/
public interface CntContent extends Remote
{
	public RecordSet<Content> getContents() throws RemoteException;
}