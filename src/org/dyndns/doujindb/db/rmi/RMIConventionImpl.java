package org.dyndns.doujindb.db.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Convention;

/**  
* RMIConventionImpl.java - RMI Implementation Convention.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RMIConventionImpl extends UnicastRemoteObject implements RMIConvention
{
	private Convention convention;
	
	protected RMIConventionImpl(Convention convention) throws RemoteException
	{
		super(1099);
		this.convention = convention;
	}

	@Override
	public String getTagName() throws RemoteException {
		return convention.getTagName();
	}

	@Override
	public String getInfo() throws RemoteException {
		return convention.getInfo();
	}

	@Override
	public String getWeblink() throws RemoteException {
		return convention.getWeblink();
	}

	@Override
	public void setTagName(String tagName) throws RemoteException {
		convention.setTagName(tagName);
	}

	@Override
	public void setInfo(String info) throws RemoteException {
		convention.setInfo(info);
	}

	@Override
	public void setWeblink(String weblink) throws RemoteException {
		convention.setWeblink(weblink);
	}

	@Override
	public RecordSet<Book> getBooks() throws RemoteException {
		return convention.getBooks();
	}

	@Override
	public void addBook(Book book) throws RemoteException {
		convention.addBook(book);
	}

	@Override
	public void removeBook(Book book) throws RemoteException {
		convention.removeBook(book);
	}
}
