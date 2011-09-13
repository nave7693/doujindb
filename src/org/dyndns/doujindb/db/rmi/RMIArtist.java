package org.dyndns.doujindb.db.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;

/**  
* RMIArtist.java - RMI Interface Artist.
* @author nozomu
* @version 1.0
*/
public interface RMIArtist extends Remote
{
	public String getJapaneseName() throws RemoteException;
	public String getTranslatedName() throws RemoteException;
	public String getRomanjiName() throws RemoteException;
	public String getWeblink() throws RemoteException;
	public void setJapaneseName(String japaneseName) throws RemoteException;
	public void setTranslatedName(String translatedName) throws RemoteException;
	public void setRomanjiName(String romanjiName) throws RemoteException;
	public void setWeblink(String weblink) throws RemoteException;
	public RecordSet<Book> getBooks() throws RemoteException;
	public RecordSet<Circle> getCircles() throws RemoteException;
	public void addBook(Book book) throws RemoteException;
	public void addCircle(Circle circle) throws RemoteException;
	public void removeBook(Book book) throws RemoteException;
	public void removeCircle(Circle circle) throws RemoteException;
}
