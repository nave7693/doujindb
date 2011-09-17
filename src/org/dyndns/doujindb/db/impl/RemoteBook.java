package org.dyndns.doujindb.db.impl;

import java.rmi.RemoteException;
import java.util.Date;
import java.io.Serializable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.db.records.Parody;

/**  
* RemoteBook.java - Remote Book.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
final class RemoteBook implements Record, Book, Serializable, Comparable<Book>
{
	private RMIBook remoteBook;

	public RemoteBook(RMIBook remoteBook) throws DataBaseException {
		this.remoteBook = remoteBook;
	}

	@Override
	public String getJapaneseName() {
		try {
			return remoteBook.getJapaneseName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getTranslatedName() {
		try {
			return remoteBook.getTranslatedName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getRomanjiName() {
		try {
			return remoteBook.getRomanjiName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setJapaneseName(String japaneseName) {
		try {
			remoteBook.setJapaneseName(japaneseName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setTranslatedName(String translatedName) {
		try {
			remoteBook.setTranslatedName(translatedName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setRomanjiName(String romanjiName) {
		try {
			remoteBook.setRomanjiName(romanjiName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public Date getDate() {
		try {
			return remoteBook.getDate();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public Type getType() {
		try {
			return remoteBook.getType();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public int getPages() {
		try {
			return remoteBook.getPages();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setPages(int pages) {
		try {
			remoteBook.setPages(pages);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setDate(Date date) {
		try {
			remoteBook.setDate(date);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setType(Type type) {
		try {
			remoteBook.setType(type);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public boolean isAdult() {
		try {
			return remoteBook.isAdult();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public boolean isDecensored() {
		try {
			return remoteBook.isDecensored();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public boolean isTranslated() {
		try {
			return remoteBook.isTranslated();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public boolean isColored() {
		try {
			return remoteBook.isColored();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setAdult(boolean adult) {
		try {
			remoteBook.setAdult(adult);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setDecensored(boolean decensored) {
		try {
			remoteBook.setDecensored(decensored);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setTranslated(boolean translated) {
		try {
			remoteBook.setTranslated(translated);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setColored(boolean colored) {
		try {
			remoteBook.setColored(colored);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public Rating getRating() {
		try {
			return remoteBook.getRating();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getInfo() {
		try {
			return remoteBook.getInfo();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setRating(Rating rating) {
		try {
			remoteBook.setRating(rating);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setInfo(String info) {
		try {
			remoteBook.setInfo(info);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Artist> getArtists() {
		try {
			return remoteBook.getArtists();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Circle> getCircles() {
		try {
			return remoteBook.getCircles();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Content> getContents() {
		try {
			return remoteBook.getContents();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public Convention getConvention() {
		try {
			return remoteBook.getConvention();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setConvention(Convention convention) {
		try {
			remoteBook.setConvention(convention);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Parody> getParodies() {
		try {
			return remoteBook.getParodies();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void addArtist(Artist artist) {
		try {
			remoteBook.addArtist(artist);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void addContent(Content content) {
		try {
			remoteBook.addContent(content);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void addParody(Parody parody) {
		try {
			remoteBook.addParody(parody);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void removeArtist(Artist artist) {
		try {
			remoteBook.removeArtist(artist);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void removeContent(Content content) {
		try {
			remoteBook.removeContent(content);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void removeParody(Parody parody) {
		try {
			remoteBook.removeParody(parody);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void doRecycle() throws DataBaseException {
		try {
			remoteBook.doRecycle();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void doRestore() throws DataBaseException {
		try {
			remoteBook.doRestore();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public boolean isRecycled() throws DataBaseException {
		try {
			return remoteBook.isRecycled();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public String getID() throws DataBaseException {
		try {
			return remoteBook.getID();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public int compareTo(Book o) {
		try {
			return remoteBook.compareTo(o);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public String toString() {
		try {
			return remoteBook.remoteToString();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
}
