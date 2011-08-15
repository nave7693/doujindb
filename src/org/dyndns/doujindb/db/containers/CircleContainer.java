package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import org.dyndns.doujindb.db.records.Circle;

/**  
* CircleContainer.java - Interface every item in the DB containing circle(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface CircleContainer extends Remote
{
	public Set<Circle> getCircles() throws RemoteException;
}
