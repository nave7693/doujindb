package org.dyndns.doujindb.db.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.db.records.Book;

/**  
* RMICircleImpl.java - RMI Implementation Circle.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RMICircleImpl extends UnicastRemoteObject implements RMICircle
{
	private Circle circle;
	
	protected RMICircleImpl(Circle circle) throws RemoteException
	{
		super(1099);
		this.circle = circle;
	}

	@Override
	public String getJapaneseName() throws RemoteException {
		return circle.getJapaneseName();
	}

	@Override
	public String getTranslatedName() throws RemoteException {
		return circle.getTranslatedName();
	}

	@Override
	public String getRomanjiName() throws RemoteException {
		return circle.getRomanjiName();
	}

	@Override
	public String getWeblink() throws RemoteException {
		return circle.getWeblink();
	}

	@Override
	public void setJapaneseName(String japaneseName) throws RemoteException {
		circle.setJapaneseName(japaneseName);
	}

	@Override
	public void setTranslatedName(String translatedName) throws RemoteException {
		circle.setTranslatedName(translatedName);
	}

	@Override
	public void setRomanjiName(String romanjiName) throws RemoteException {
		circle.setRomanjiName(romanjiName);
	}

	@Override
	public void setWeblink(String weblink) throws RemoteException {
		circle.setWeblink(weblink);
	}

	@Override
	public RecordSet<Artist> getArtists() throws RemoteException {
		return circle.getArtists();
	}

	@Override
	public RecordSet<Book> getBooks() throws RemoteException {
		return circle.getBooks();
	}

	@Override
	public void addArtist(Artist artist) throws RemoteException {
		circle.addArtist(artist);
	}

	@Override
	public void removeArtist(Artist artist) throws RemoteException {
		circle.removeArtist(artist);
	}
}
