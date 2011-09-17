package org.dyndns.doujindb.db.impl;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* RemoteCircle.java - Remote Circle.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
final class RemoteCircle implements Record, Circle, Serializable, Comparable<Circle>
{
	private RMICircle remoteCircle;

	public RemoteCircle(RMICircle remoteCircle) throws DataBaseException {
		this.remoteCircle = remoteCircle;
	}

	@Override
	public String getJapaneseName() throws DataBaseException {
		try {
			return remoteCircle.getJapaneseName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getTranslatedName() throws DataBaseException {
		try {
			return remoteCircle.getTranslatedName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getRomanjiName() throws DataBaseException {
		try {
			return remoteCircle.getRomanjiName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getWeblink() throws DataBaseException {
		try {
			return remoteCircle.getWeblink();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setJapaneseName(String japaneseName) throws DataBaseException {
		try {
			remoteCircle.setJapaneseName(japaneseName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setTranslatedName(String translatedName) throws DataBaseException {
		try {
			remoteCircle.setTranslatedName(translatedName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setRomanjiName(String romanjiName) throws DataBaseException {
		try {
			remoteCircle.setRomanjiName(romanjiName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setWeblink(String weblink) throws DataBaseException {
		try {
			remoteCircle.setWeblink(weblink);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Artist> getArtists() throws DataBaseException {
		try {
			return remoteCircle.getArtists();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Book> getBooks() throws DataBaseException {
		try {
			return remoteCircle.getBooks();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void addArtist(Artist artist) throws DataBaseException {
		try {
			remoteCircle.addArtist(artist);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void removeArtist(Artist artist) throws DataBaseException {
		try {
			remoteCircle.removeArtist(artist);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRecycle() throws DataBaseException {
		try {
			remoteCircle.doRecycle();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRestore() throws DataBaseException {
		try {
			remoteCircle.doRestore();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public boolean isRecycled() throws DataBaseException {
		try {
			return remoteCircle.isRecycled();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public String getID() throws DataBaseException {
		try {
			return remoteCircle.getID();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public int compareTo(Circle o) {
		try {
			return remoteCircle.compareTo(o);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
}