package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

final class ParodyImpl extends RecordImpl implements Parody, Serializable//, Comparable<Parody>
{
	private static final long serialVersionUID = 0xFEED0001L;

	public ParodyImpl(org.dyndns.doujindb.db.cayenne.Parody ref) throws DataBaseException
	{
		if(ref == null)
			throw new IllegalArgumentException("CayenneDataObject reference can't be null.");
		this.ref = ref;
		doRestore();
	}

	@Override
	public synchronized String getJapaneseName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Parody)ref).getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Parody)ref).setJapaneseName(japaneseName);
	}

	@Override
	public synchronized String getTranslatedName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Parody)ref).getTranslatedName();
	}

	@Override
	public synchronized void setTranslatedName(String translatedName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Parody)ref).setTranslatedName(translatedName);
	}

	@Override
	public synchronized String getRomanjiName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Parody)ref).getRomanjiName();
	}

	@Override
	public synchronized void setRomanjiName(String romanjiName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Parody)ref).setRomanjiName(romanjiName);
	}

	@Override
	public synchronized String getWeblink() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Parody)ref).getWeblink();
	}

	@Override
	public synchronized void setWeblink(String weblink) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Parody)ref).setWeblink(weblink);
	}

	@Override
	public synchronized RecordSet<Book> getBooks() throws DataBaseException
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ((org.dyndns.doujindb.db.cayenne.Parody)ref).getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
			if(!r.getRecycled())
				try { set.add(new RemoteBook(new RMIBookImpl(new BookImpl(r)))); } catch (RemoteException re) { }
		return new RecordSetImpl<Book>(set);
	}

	@Override
	public synchronized String toString()
	{
		return this.getJapaneseName();
		/*return japaneseName + 
			(romanjiName.equals("") ? "" : " ("+romanjiName+")") +
			(translatedName.equals("") ? "" : " ("+translatedName+")");*/
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
		((org.dyndns.doujindb.db.cayenne.Parody)ref).addToBooks(refBook);
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
		((org.dyndns.doujindb.db.cayenne.Parody)ref).removeFromBooks(refBook);
	}
	
	@Override
	public synchronized String getID() throws DataBaseException
	{
		//return "P" + super.getID();
		return "P" + ((org.dyndns.doujindb.db.cayenne.Parody)ref).getID();
	}
	
	@Override
	public void doRecycle() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Parody)ref).setRecycled(true);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Parody)ref).setRecycled(false);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Parody)ref).getRecycled();
	}
}
