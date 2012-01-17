package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

final class ContentImpl extends RecordImpl implements Content, Serializable//, Comparable<Content>
{
	private static final long serialVersionUID = 0xFEED0001L;

	public ContentImpl(org.dyndns.doujindb.db.cayenne.Content ref) throws DataBaseException
	{
		this.ref = ref;
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
			set.add(new BookImpl(r));
		return new RecordSetImpl<Book>(set);
	}
	
	@Override
	public synchronized Set<String> getAliases() throws DataBaseException
	{
		Set<String> set = new TreeSet<String>();
		Set<org.dyndns.doujindb.db.cayenne.ContentAlias> result = ((org.dyndns.doujindb.db.cayenne.Content)ref).getAliases();
		for(org.dyndns.doujindb.db.cayenne.ContentAlias r : result)
			set.add(r.getTagName());
		return set;
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
		((org.dyndns.doujindb.db.cayenne.Content)ref).addToBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
	}

	@Override
	public void removeBook(Book book) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Content)ref).removeFromBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
	}
	
	@Override
	public void addAlias(String alias) throws DataBaseException
	{
		if(getAliases().contains(alias))
			return;
		((org.dyndns.doujindb.db.cayenne.Content)ref).addToAliases(
			((DataBaseImpl)DataBaseImpl.getInstance()).context.newObject(org.dyndns.doujindb.db.cayenne.ContentAlias.class)
		);
	}

	@Override
	public void removeAlias(String alias) throws DataBaseException
	{
		Set<org.dyndns.doujindb.db.cayenne.ContentAlias> result = ((org.dyndns.doujindb.db.cayenne.Content)ref).getAliases();
		for(org.dyndns.doujindb.db.cayenne.ContentAlias r : result)
			if(r.getTagName().equals(alias))
				((org.dyndns.doujindb.db.cayenne.Content)ref).removeFromAliases(r);
	}
	
	@Override
	public synchronized String getID() throws DataBaseException
	{
		Integer ID = ((org.dyndns.doujindb.db.cayenne.Content)ref).getID();
		if(ID == null)
			return null;
		else
			return "T" + String.format("%08x", ID);
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
