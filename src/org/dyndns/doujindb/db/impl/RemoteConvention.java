package org.dyndns.doujindb.db.impl;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* RemoteConvention.java - Remote Convention.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
final class RemoteConvention implements Record, Convention, Serializable, Comparable<Convention>
{
	private RMIConvention remoteConvention;

	public RemoteConvention(RMIConvention remoteConvention) throws DataBaseException {
		this.remoteConvention = remoteConvention;
	}

	@Override
	public String getTagName() throws DataBaseException {
		try {
			return remoteConvention.getTagName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getInfo() throws DataBaseException {
		try {
			return remoteConvention.getInfo();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setTagName(String tagName) throws DataBaseException {
		try {
			remoteConvention.setTagName(tagName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setInfo(String info) throws DataBaseException {
		try {
			remoteConvention.setInfo(info);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Book> getBooks() throws DataBaseException {
		try {
			return remoteConvention.getBooks();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void addBook(Book book) throws DataBaseException {
		try {
			remoteConvention.addBook(book);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void removeBook(Book book) throws DataBaseException {
		try {
			remoteConvention.removeBook(book);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRecycle() throws DataBaseException {
		try {
			remoteConvention.doRecycle();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRestore() throws DataBaseException {
		try {
			remoteConvention.doRestore();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public boolean isRecycled() throws DataBaseException {
		try {
			return remoteConvention.isRecycled();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getWeblink() throws DataBaseException {
		try {
			return remoteConvention.getWeblink();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setWeblink(String weblink) throws DataBaseException {
		try {
			remoteConvention.setWeblink(weblink);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public String getID() throws DataBaseException {
		try {
			return remoteConvention.getID();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public int compareTo(Convention o) {
		try {
			return remoteConvention.compareTo(o);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public String toString() {
		try {
			return remoteConvention.remoteToString();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
}
