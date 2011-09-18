package org.dyndns.doujindb.db.impl;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;

/**  
* RemoteArtist.java - Remote Artist.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
final class RemoteArtist implements Record, Artist, Serializable, Comparable<Artist>
{
	private RMIArtist remoteArtist;

	public RemoteArtist(RMIArtist remoteArtist) throws DataBaseException {
		this.remoteArtist = remoteArtist;
	}

	@Override
	public String getJapaneseName() throws DataBaseException {
		try {
			return remoteArtist.getJapaneseName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getTranslatedName() throws DataBaseException {
		try {
			return remoteArtist.getTranslatedName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getRomanjiName() throws DataBaseException {
		try {
			return remoteArtist.getRomanjiName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getWeblink() throws DataBaseException {
		try {
			return remoteArtist.getWeblink();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setJapaneseName(String japaneseName) throws DataBaseException {
		try {
			remoteArtist.setJapaneseName(japaneseName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setTranslatedName(String translatedName) throws DataBaseException {
		try {
			remoteArtist.setTranslatedName(translatedName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setRomanjiName(String romanjiName) throws DataBaseException {
		try {
			remoteArtist.setRomanjiName(romanjiName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setWeblink(String weblink) throws DataBaseException {
		try {
			remoteArtist.setWeblink(weblink);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Book> getBooks() throws DataBaseException {
		try {
			return remoteArtist.getBooks();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Circle> getCircles() throws DataBaseException {
		try {
			return remoteArtist.getCircles();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void addBook(Book book) throws DataBaseException {
		try {
			remoteArtist.addBook(book);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void addCircle(Circle circle) throws DataBaseException {
		try {
			remoteArtist.addCircle(circle);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void removeBook(Book book) throws DataBaseException {
		try {
			remoteArtist.removeBook(book);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void removeCircle(Circle circle) throws DataBaseException {
		try {
			remoteArtist.removeCircle(circle);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRecycle() throws DataBaseException {
		try {
			remoteArtist.doRecycle();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRestore() throws DataBaseException {
		try {
			remoteArtist.doRestore();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public boolean isRecycled() throws DataBaseException {
		try {
			return remoteArtist.isRecycled();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getID() throws DataBaseException {
		try {
			return remoteArtist.getID();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public int compareTo(Artist o) {
		try {
			return remoteArtist.compareTo(o);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String toString() {
		try {
			return remoteArtist.remoteToString();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public boolean equals(Object obj) {
		try {
			return remoteArtist.remoteEquals(obj);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
}
