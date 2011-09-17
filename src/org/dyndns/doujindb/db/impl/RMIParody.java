package org.dyndns.doujindb.db.impl;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* RMIParody.java - RMI Interface Parody.
* @author nozomu
* @version 1.0
*/
public interface RMIParody extends Remote
{
	public String getID() throws RemoteException;
	public String getJapaneseName() throws RemoteException;
	public String getTranslatedName() throws RemoteException;
	public String getRomanjiName() throws RemoteException;
	public String getWeblink() throws RemoteException;
	public void setJapaneseName(String japaneseName) throws RemoteException;
	public void setTranslatedName(String translatedName) throws RemoteException;
	public void setRomanjiName(String romanjiName) throws RemoteException;
	public void setWeblink(String weblink) throws RemoteException;
	public RecordSet<Book> getBooks() throws RemoteException;
	public void addBook(Book book) throws RemoteException;
	public void removeBook(Book book) throws RemoteException;
	public boolean isRecycled() throws RemoteException;
	public void doRestore() throws RemoteException;
	public void doRecycle() throws RemoteException;
	public int compareTo(Parody o) throws RemoteException;
	public String remoteToString() throws RemoteException;
}
