package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.*;

/**  
* CntParody.java - Interface every item in the DB containing parody(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface CntParody extends Remote
{
	public RecordSet<Parody> getParodies() throws RemoteException;
}
