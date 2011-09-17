package org.dyndns.doujindb.db.impl;

import java.util.Date;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;
import org.dyndns.doujindb.db.records.Book.*;

/**  
* RMIBookImpl.java - RMI Implementation Book.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RMIBookImpl extends UnicastRemoteObject implements RMIBook
{
	private BookImpl book;
	
	protected RMIBookImpl(BookImpl book) throws RemoteException
	{
		super(1099);
		this.book = book;
	}
	
	@Override
	public synchronized String getID() throws RemoteException
	{
		return book.getID();
	}

	@Override
	public String getJapaneseName() throws RemoteException {
		return book.getJapaneseName();
	}

	@Override
	public String getTranslatedName() throws RemoteException {
		return book.getTranslatedName();
	}

	@Override
	public String getRomanjiName() throws RemoteException {
		return book.getRomanjiName();
	}

	@Override
	public void setJapaneseName(String setJapaneseName) throws RemoteException {
		book.setJapaneseName(setJapaneseName);
	}

	@Override
	public void setTranslatedName(String translatedName) throws RemoteException {
		book.setTranslatedName(translatedName);
	}

	@Override
	public void setRomanjiName(String romanjiName) throws RemoteException {
		book.setRomanjiName(romanjiName);
	}

	@Override
	public Date getDate() throws RemoteException {
		return book.getDate();
	}

	@Override
	public Type getType() throws RemoteException {
		return book.getType();
	}

	@Override
	public int getPages() throws RemoteException {
		return book.getPages();
	}

	@Override
	public void setPages(int pages) throws RemoteException {
		book.setPages(pages);
	}

	@Override
	public void setDate(Date date) throws RemoteException {
		book.setDate(date);
	}

	@Override
	public void setType(Type type) throws RemoteException {
		book.setType(type);
	}

	@Override
	public boolean isAdult() throws RemoteException {
		return book.isAdult();
	}

	@Override
	public boolean isDecensored() throws RemoteException {
		return book.isDecensored();
	}

	@Override
	public boolean isTranslated() throws RemoteException {
		return book.isTranslated();
	}

	@Override
	public boolean isColored() throws RemoteException {
		return book.isColored();
	}

	@Override
	public void setAdult(boolean adult) throws RemoteException {
		book.setAdult(adult);
	}

	@Override
	public void setDecensored(boolean decensored) throws RemoteException {
		book.setDecensored(decensored);
	}

	@Override
	public void setTranslated(boolean translated) throws RemoteException {
		book.setTranslated(translated);
	}

	@Override
	public void setColored(boolean colored) throws RemoteException {
		book.setColored(colored);
	}

	@Override
	public Rating getRating() throws RemoteException {
		return book.getRating();
	}

	@Override
	public String getInfo() throws RemoteException {
		return book.getInfo();
	}

	@Override
	public void setRating(Rating rating) throws RemoteException {
		book.setRating(rating);
	}

	@Override
	public void setInfo(String info) throws RemoteException {
		book.setInfo(info);
	}

	@Override
	public RecordSet<Artist> getArtists() throws RemoteException {
		return book.getArtists();
	}

	@Override
	public RecordSet<Circle> getCircles() throws RemoteException {
		return book.getCircles();
	}

	@Override
	public RecordSet<Content> getContents() throws RemoteException {
		return book.getContents();
	}

	@Override
	public Convention getConvention() throws RemoteException {
		return book.getConvention();
	}

	@Override
	public void setConvention(Convention convention) throws RemoteException {
		book.setConvention(convention);
	}

	@Override
	public RecordSet<Parody> getParodies() throws RemoteException {
		return book.getParodies();
	}

	@Override
	public void addArtist(Artist artist) throws RemoteException {
		book.addArtist(artist);
	}

	@Override
	public void addContent(Content content) throws RemoteException {
		book.addContent(content);
	}

	@Override
	public void addParody(Parody parody) throws RemoteException {
		book.addParody(parody);
	}

	@Override
	public void removeArtist(Artist artist) throws RemoteException {
		book.removeArtist(artist);
	}

	@Override
	public void removeContent(Content content) throws RemoteException {
		book.removeContent(content);
	}

	@Override
	public void removeParody(Parody parody) throws RemoteException {
		book.removeParody(parody);
	}

	@Override
	public boolean isRecycled() throws RemoteException {
		return book.isRecycled();
	}

	@Override
	public void doRestore() throws RemoteException {
		book.doRestore();
	}

	@Override
	public void doRecycle() throws RemoteException {
		book.doRecycle();
	}
	
	@Override
	public int compareTo(Book o) throws RemoteException {
		return book.compareTo(o);
	}
}
