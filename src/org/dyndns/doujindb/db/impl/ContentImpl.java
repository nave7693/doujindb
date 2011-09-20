package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

final class ContentImpl extends RecordImpl implements Content, Serializable//, Comparable<Content>
{
	private static final long serialVersionUID = 0xFEED0001L;

	public ContentImpl(org.dyndns.doujindb.db.cayenne.Content ref) throws DataBaseException
	{
		if(ref == null)
			throw new IllegalArgumentException("CayenneDataObject reference can't be null.");
		this.ref = ref;
		doRestore();
	}

	@Override
	public synchronized String getTagName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Content)ref).getTagName();
	}

	@Override
	public synchronized void setTagName(String tagName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Content)ref).setTagName(tagName);
	}

	@Override
	public synchronized String getInfo() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Content)ref).getInfo();
	}

	@Override
	public synchronized void setInfo(String info) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Content)ref).setInfo(info);
	}
	
	@Override
	public synchronized RecordSet<Book> getBooks() throws DataBaseException
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ((org.dyndns.doujindb.db.cayenne.Content)ref).getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
			try { set.add(new RemoteBook(new RMIBookImpl(new BookImpl(r)))); } catch (RemoteException re) { }
		return new RecordSetImpl<Book>(set);
	}
	
	@Override
	public synchronized String toString()
	{
		return this.getTagName();
	}

	@Override
	public void addBook(Book book) throws DataBaseException
	{
		if(getBooks().contains(book))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Book.class,
				ExpressionFactory.inDbExp("ID", ((RemoteBook)book).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Book refBook = (org.dyndns.doujindb.db.cayenne.Book) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Content)ref).addToBooks(refBook);
	}

	@Override
	public void removeBook(Book book) throws DataBaseException
	{
		if(!getBooks().contains(book))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Book.class,
				ExpressionFactory.inDbExp("ID", ((RemoteBook)book).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Book refBook = (org.dyndns.doujindb.db.cayenne.Book) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Content)ref).removeFromBooks(refBook);
	}
	
	@Override
	public synchronized String getID() throws DataBaseException
	{
		//return "T" + super.getID();
		return "T" + ((org.dyndns.doujindb.db.cayenne.Content)ref).getID();
	}
	
	@Override
	public void doRecycle() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Content)ref).setRecycled(true);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Content)ref).setRecycled(false);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Content)ref).getRecycled();
	}
}
