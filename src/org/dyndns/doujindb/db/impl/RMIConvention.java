package org.dyndns.doujindb.db.impl;

import java.rmi.Remote;
import java.rmi.RemoteException;


import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* RMIConvention.java - RMI Interface Convention.
* @author nozomu
* @version 1.0
*/
public interface RMIConvention extends Remote
{
	public String getID() throws RemoteException;
	public String getTagName() throws RemoteException;
	public String getInfo() throws RemoteException;
	public String getWeblink() throws RemoteException;
	public void setTagName(String tagName) throws RemoteException;
	public void setInfo(String info) throws RemoteException;
	public void setWeblink(String weblink) throws RemoteException;
	public RecordSet<Book> getBooks() throws RemoteException;
	public void addBook(Book book) throws RemoteException;
	public void removeBook(Book book) throws RemoteException;
	public boolean isRecycled() throws RemoteException;
	public void doRestore() throws RemoteException;
	public void doRecycle() throws RemoteException;
	public int compareTo(Convention o) throws RemoteException;
}
