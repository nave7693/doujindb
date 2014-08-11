package org.dyndns.doujindb.db;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.record.*;

final class ContentImpl implements Content, Serializable
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	org.dyndns.doujindb.db.cayenne.Content ref;

	public ContentImpl(org.dyndns.doujindb.db.cayenne.Content ref) throws DataBaseException
	{
		this.ref = ref;
	}

	@Override
	public synchronized String getTagName() throws DataBaseException
	{
		return ref.getTagName();
	}

	@Override
	public synchronized void setTagName(String tagName) throws DataBaseException
	{
		if(getTagName().equals(tagName))
			return;
		ref.setTagName(tagName);
		DataBase.fireRecordUpdated(this, UpdateData.property("tag_name"));
	}

	@Override
	public synchronized String getInfo() throws DataBaseException
	{
		return ref.getInfo();
	}

	@Override
	public synchronized void setInfo(String info) throws DataBaseException
	{
		if(getInfo().equals(info))
			return;
		ref.setInfo(info);
		DataBase.fireRecordUpdated(this, UpdateData.property("info"));
	}
	
	@Override
	public synchronized RecordSet<Book> getBooks() throws DataBaseException
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ref.getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
				set.add(new BookImpl(r));
		return new RecordSetImpl<Book>(set);
	}
	
	@Override
	public synchronized Set<String> getAliases() throws DataBaseException
	{
		Set<String> set = new TreeSet<String>();
		Set<org.dyndns.doujindb.db.cayenne.ContentAlias> result = ref.getAliases();
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
		ref.addToBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.BookImpl)book).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.link(book));
		DataBase.fireRecordUpdated(book, UpdateData.link(this));
	}

	@Override
	public void removeBook(Book book) throws DataBaseException
	{
		ref.removeFromBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.BookImpl)book).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.unlink(book));
		DataBase.fireRecordUpdated(book, UpdateData.unlink(this));
	}
	
	@Override
	public void addAlias(String alias) throws DataBaseException
	{
		if(getAliases().contains(alias))
			return;
		org.dyndns.doujindb.db.cayenne.ContentAlias object = DataBase.newContentAlias();
		object.setTagName(alias);
		ref.addToAliases(object);
	}

	@Override
	public void removeAlias(String alias) throws DataBaseException
	{
		Set<org.dyndns.doujindb.db.cayenne.ContentAlias> set = ref.getAliases();
		synchronized(set)
		{
			Iterator<org.dyndns.doujindb.db.cayenne.ContentAlias> i = set.iterator();
			while(i.hasNext())
			{
				org.dyndns.doujindb.db.cayenne.ContentAlias a = i.next();
				if(a.getTagName().equals(alias))
				{
					i.remove();
					ref.removeFromAliases(a);
					DataBase.deleteObject(a);
				}
			}
		}
	}
	
	@Override
	public synchronized Integer getId() throws DataBaseException
	{
		return ref.getID();
	}
	
	@Override
	public void doRecycle() throws DataBaseException
	{
		ref.setRecycled(true);
		DataBase.fireRecordRecycled(this);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		ref.setRecycled(false);
		DataBase.fireRecordRestored(this);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ref.getRecycled();
	}

	@Override
	public void removeAll() throws DataBaseException
	{
		for(Book book : getBooks())
			removeBook(book);
	}

	@Override
	public int compareTo(Content o) {
		return this.getId().compareTo(o.getId());
	}
}
