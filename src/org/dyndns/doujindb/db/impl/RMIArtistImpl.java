package org.dyndns.doujindb.db.impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;

/**  
* RMIArtistImpl.java - RMI Implementation Artist.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RMIArtistImpl extends UnicastRemoteObject implements RMIArtist
{
	private ArtistImpl artist;
	
	public RMIArtistImpl(ArtistImpl artist) throws RemoteException
	{
		super(1099);
		this.artist = artist;
	}
	
	@Override
	public synchronized String getID() throws RemoteException
	{
		return artist.getID();
	}
	
	@Override
	public String getJapaneseName() throws RemoteException {
		return artist.getJapaneseName();
	}

	@Override
	public String getTranslatedName() throws RemoteException {
		return artist.getTranslatedName();
	}

	@Override
	public String getRomanjiName() throws RemoteException {
		return artist.getRomanjiName();
	}

	@Override
	public String getWeblink() throws RemoteException {
		return artist.getWeblink();
	}

	@Override
	public void setJapaneseName(String japaneseName) throws RemoteException {
		artist.setJapaneseName(japaneseName);
	}

	@Override
	public void setTranslatedName(String translatedName) throws RemoteException {
		artist.setTranslatedName(translatedName);
	}

	@Override
	public void setRomanjiName(String romanjiName) throws RemoteException {
		artist.setRomanjiName(romanjiName);
	}

	@Override
	public void setWeblink(String weblink) throws RemoteException {
		artist.setWeblink(weblink);
	}

	@Override
	public RecordSet<Book> getBooks() throws RemoteException {
		return artist.getBooks();
	}

	@Override
	public RecordSet<Circle> getCircles() throws RemoteException {
		return artist.getCircles();
	}

	@Override
	public void addBook(Book book) throws RemoteException {
		artist.addBook(book);
	}

	@Override
	public void addCircle(Circle circle) throws RemoteException {
		artist.addCircle(circle);
	}

	@Override
	public void removeBook(Book book) throws RemoteException {
		artist.removeBook(book);
	}

	@Override
	public void removeCircle(Circle circle) throws RemoteException {
		artist.removeCircle(circle);
	}

	@Override
	public boolean isRecycled() throws RemoteException {
		return artist.isRecycled();
	}

	@Override
	public void doRestore() throws RemoteException {
		artist.doRestore();
	}

	@Override
	public void doRecycle() throws RemoteException {
		artist.doRecycle();
	}

	@Override
	public int compareTo(Artist o) throws RemoteException {
		return artist.compareTo(o);
	}
}
