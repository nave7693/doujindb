package org.dyndns.doujindb.db.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.db.records.Book;

/**  
* RMIContentImpl.java - RMI Implementation Content.
* @author nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RMIContentImpl extends UnicastRemoteObject implements RMIContent
{
	private Content content;
	
	protected RMIContentImpl(Content content) throws RemoteException
	{
		super(1099);
		this.content = content;
	}
	

	@Override
	public String getTagName() throws RemoteException {
		return content.getTagName();
	}

	@Override
	public String getInfo() throws RemoteException {
		return content.getInfo();
	}

	@Override
	public void setTagName(String tagName) throws RemoteException {
		content.setTagName(tagName);
	}

	@Override
	public void setInfo(String info) throws RemoteException {
		content.setInfo(info);
	}

	@Override
	public RecordSet<Book> getBooks() throws RemoteException {
		return content.getBooks();
	}

	@Override
	public void addBook(Book book) throws RemoteException {
		content.addBook(book);
	}

	@Override
	public void removeBook(Book book) throws RemoteException {
		content.removeBook(book);
	}
	
	@Override
	public boolean isRecycled() throws RemoteException {
		return content.isRecycled();
	}

	@Override
	public void doRestore() throws RemoteException {
		content.doRestore();
	}

	@Override
	public void doRecycle() throws RemoteException {
		content.doRecycle();
	}
}
