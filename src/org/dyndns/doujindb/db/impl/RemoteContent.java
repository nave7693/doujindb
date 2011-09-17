package org.dyndns.doujindb.db.impl;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* RemoteContent.java - Remote Content.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
final class RemoteContent implements Record, Content, Serializable, Comparable<Content>
{
	private RMIContent remoteContent;

	public RemoteContent(RMIContent remoteContent) throws DataBaseException {
		this.remoteContent = remoteContent;
	}

	@Override
	public String getTagName() throws DataBaseException {
		try {
			return remoteContent.getTagName();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public String getInfo() throws DataBaseException {
		try {
			return remoteContent.getInfo();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setTagName(String tagName) throws DataBaseException {
		try {
			remoteContent.setTagName(tagName);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void setInfo(String info) throws DataBaseException {
		try {
			remoteContent.setInfo(info);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Book> getBooks() throws DataBaseException {
		try {
			return remoteContent.getBooks();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void addBook(Book book) throws DataBaseException {
		try {
			remoteContent.addBook(book);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void removeBook(Book book) throws DataBaseException {
		try {
			remoteContent.removeBook(book);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRecycle() throws DataBaseException {
		try {
			remoteContent.doRecycle();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public void doRestore() throws DataBaseException {
		try {
			remoteContent.doRestore();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public boolean isRecycled() throws DataBaseException {
		try {
			return remoteContent.isRecycled();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public String getID() throws DataBaseException {
		try {
			return remoteContent.getID();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public int compareTo(Content o) {
		try {
			return remoteContent.compareTo(o);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
	
	@Override
	public String toString() {
		try {
			return remoteContent.remoteToString();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
}
