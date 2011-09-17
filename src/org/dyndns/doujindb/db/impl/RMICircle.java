package org.dyndns.doujindb.db.impl;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* RMICircle.java - RMI Interface Circle.
* @author nozomu
* @version 1.0
*/
public interface RMICircle extends Remote
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
	public RecordSet<Artist> getArtists() throws RemoteException;
	public RecordSet<Book> getBooks() throws RemoteException;
	public void addArtist(Artist artist) throws RemoteException;
	public void removeArtist(Artist artist) throws RemoteException;
	public boolean isRecycled() throws RemoteException;
	public void doRestore() throws RemoteException;
	public void doRecycle() throws RemoteException;
	public int compareTo(Circle o) throws RemoteException;
}
