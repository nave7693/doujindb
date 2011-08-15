package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import org.dyndns.doujindb.db.records.Book;

/**  
* BookContainer.java - Interface every item in the DB containing book(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface BookContainer extends Remote
{
	public Set<Book> getBooks() throws RemoteException;
}
