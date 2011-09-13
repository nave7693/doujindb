package org.dyndns.doujindb.db.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.db.records.Book;

/**  
* RMIParodyImpl.java - RMI Implementation Parody.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RMIParodyImpl extends UnicastRemoteObject implements RMIParody
{
	private Parody parody;
	
	protected RMIParodyImpl(Parody parody) throws RemoteException
	{
		super(1099);
		this.parody = parody;
	}

	@Override
	public String getJapaneseName() throws RemoteException {
		return parody.getJapaneseName();
	}

	@Override
	public String getTranslatedName() throws RemoteException {
		return parody.getTranslatedName();
	}

	@Override
	public String getRomanjiName() throws RemoteException {
		return parody.getRomanjiName();
	}

	@Override
	public String getWeblink() throws RemoteException {
		return parody.getWeblink();
	}

	@Override
	public void setJapaneseName(String japaneseName) throws RemoteException {
		parody.setJapaneseName(japaneseName);
	}

	@Override
	public void setTranslatedName(String translatedName) throws RemoteException {
		parody.setTranslatedName(translatedName);
	}

	@Override
	public void setRomanjiName(String romanjiName) throws RemoteException {
		parody.setRomanjiName(romanjiName);
	}

	@Override
	public void setWeblink(String weblink) throws RemoteException {
		parody.setWeblink(weblink);
	}

	@Override
	public RecordSet<Book> getBooks() throws RemoteException {
		return parody.getBooks();
	}

	@Override
	public void addBook(Book book) throws RemoteException {
		parody.addBook(book);
	}

	@Override
	public void removeBook(Book book) throws RemoteException {
		parody.removeBook(book);
	}
}
