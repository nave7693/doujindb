package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* BookContainer.java - Interface every item in the DB containing book(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface BookContainer extends Remote
{
	public RecordSet<Book> getBooks() throws RemoteException;
}
