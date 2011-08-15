package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.rmi.*;
import java.util.Set;

import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.BookContainer;

/**  
* Parody.java - Interface Parody.
* @author nozomu
* @version 1.0
*/
public interface Parody extends Record, Remote, Serializable, BookContainer
{
	public String getJapaneseName() throws RemoteException;
	public String getTranslatedName() throws RemoteException;
	public String getRomanjiName() throws RemoteException;
	public String getWeblink() throws RemoteException;
	public void setJapaneseName(String japaneseName) throws RemoteException;
	public void setTranslatedName(String translatedName) throws RemoteException;
	public void setRomanjiName(String romanjiName) throws RemoteException;
	public void setWeblink(String weblink) throws RemoteException;
	public Set<Book> getBooks() throws RemoteException;
}
