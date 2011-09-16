package org.dyndns.doujindb.db.impl;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Parody;

/**  
* RemoteParody.java - Remote Parody.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
final class RemoteParody extends RecordImpl implements Parody, Serializable//, Comparable<Parody>
{
	private RMIParody remoteParody;

	public RemoteParody(RMIParody remoteParody) throws DataBaseException {
		this.remoteParody = remoteParody;
	}

	@Override
	public String getJapaneseName() throws DataBaseException {
		try {
			return remoteParody.getJapaneseName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getTranslatedName() throws DataBaseException {
		try {
			return remoteParody.getTranslatedName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getRomanjiName() throws DataBaseException {
		try {
			return remoteParody.getRomanjiName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getWeblink() throws DataBaseException {
		try {
			return remoteParody.getWeblink();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setJapaneseName(String japaneseName) throws DataBaseException {
		try {
			remoteParody.setJapaneseName(japaneseName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setTranslatedName(String translatedName) throws DataBaseException {
		try {
			remoteParody.setTranslatedName(translatedName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setRomanjiName(String romanjiName) throws DataBaseException {
		try {
			remoteParody.setRomanjiName(romanjiName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setWeblink(String weblink) throws DataBaseException {
		try {
			remoteParody.setWeblink(weblink);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Book> getBooks() throws DataBaseException {
		try {
			return remoteParody.getBooks();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRecycle() throws DataBaseException {
		try {
			remoteParody.doRecycle();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRestore() throws DataBaseException {
		try {
			remoteParody.doRestore();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public boolean isRecycled() throws DataBaseException {
		try {
			return remoteParody.isRecycled();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void addBook(Book book) throws DataBaseException {
		try {
			remoteParody.addBook(book);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void removeBook(Book book) throws DataBaseException {
		try {
			remoteParody.removeBook(book);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
}
