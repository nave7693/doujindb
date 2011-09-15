package org.dyndns.doujindb.db.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Book;

/**  
* RMIContent.java - RMI Interface Content.
* @author nozomu
* @version 1.0
*/
public interface RMIContent extends Remote
{
	public String getTagName() throws RemoteException;
	public String getInfo() throws RemoteException;
	public void setTagName(String tagName) throws RemoteException;
	public void setInfo(String info) throws RemoteException;
	public RecordSet<Book> getBooks() throws RemoteException;
	public void addBook(Book book) throws RemoteException;
	public void removeBook(Book book) throws RemoteException;
	public boolean isRecycled() throws RemoteException;
	public void doRestore() throws RemoteException;
	public void doRecycle() throws RemoteException;
}
