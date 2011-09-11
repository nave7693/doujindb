package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.*;

/**  
* CntCircle.java - Interface every item in the DB containing circle(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface CntCircle extends Remote
{
	public RecordSet<Circle> getCircles() throws RemoteException;
}
