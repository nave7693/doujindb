package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.*;

final class ConventionImpl extends RecordImpl implements Convention, Serializable//, Comparable<Convention>
{
	private static final long serialVersionUID = 0xFEED0001L;

	public ConventionImpl(org.dyndns.doujindb.db.cayenne.Convention ref) throws DataBaseException
	{
		this.ref = ref;
	}

	@Override
	public synchronized String getTagName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Convention)ref).getTagName();
	}

	@Override
	public synchronized void setTagName(String tagName) throws DataBaseException
	{
		if(getTagName().equals(tagName))
			return;
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setTagName(tagName);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("tag_name"));
	}

	@Override
	public synchronized String getInfo() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Convention)ref).getInfo();
	}

	@Override
	public synchronized void setInfo(String info) throws DataBaseException
	{
		if(getInfo().equals(info))
			return;
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setInfo(info);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("info"));
	}	
	
	@Override
	public synchronized String getWeblink() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Convention)ref).getWeblink();
	}

	@Override
	public synchronized void setWeblink(String weblink) throws DataBaseException
	{
		if(getWeblink().equals(weblink))
			return;
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setWeblink(weblink);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("weblink"));
	}

	@Override
	public synchronized RecordSet<Book> getBooks() throws DataBaseException
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ((org.dyndns.doujindb.db.cayenne.Convention)ref).getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
				set.add(new BookImpl(r));
		return new RecordSetImpl<Book>(set);
	}
	
	@Override
	public synchronized Set<String> getAliases() throws DataBaseException
	{
		Set<String> set = new TreeSet<String>();
		Set<org.dyndns.doujindb.db.cayenne.ConventionAlias> result = ((org.dyndns.doujindb.db.cayenne.Convention)ref).getAliases();
		for(org.dyndns.doujindb.db.cayenne.ConventionAlias r : result)
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
		((org.dyndns.doujindb.db.cayenne.Convention)ref).addToBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.link(book));
		((DataBaseImpl)Core.Database)._recordUpdated(book, UpdateData.link(this));
	}

	@Override
	public void removeBook(Book book) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Convention)ref).removeFromBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(book));
		((DataBaseImpl)Core.Database)._recordUpdated(book, UpdateData.unlink(this));
	}
	
	@Override
	public void addAlias(String alias) throws DataBaseException
	{
		if(getAliases().contains(alias))
			return;
		org.dyndns.doujindb.db.cayenne.ConventionAlias object = ((DataBaseImpl)DataBaseImpl.getInstance()).context.newObject(org.dyndns.doujindb.db.cayenne.ConventionAlias.class);
		object.setTagName(alias);
		((org.dyndns.doujindb.db.cayenne.Convention)ref).addToAliases(
				object
		);
	}

	@Override
	public void removeAlias(String alias) throws DataBaseException
	{
		Set<org.dyndns.doujindb.db.cayenne.ConventionAlias> set = ((org.dyndns.doujindb.db.cayenne.Convention)ref).getAliases();
		synchronized(set)
		{
			Iterator<org.dyndns.doujindb.db.cayenne.ConventionAlias> i = set.iterator();
			while(i.hasNext())
			{
				org.dyndns.doujindb.db.cayenne.ConventionAlias a = i.next();
				if(a.getTagName().equals(alias))
				{
					((org.dyndns.doujindb.db.cayenne.Convention)ref).removeFromAliases(a);
					((DataBaseImpl)DataBaseImpl.getInstance()).context.deleteObject(a);
				}
			}
		}
	}
	
	@Override
	public synchronized String getID() throws DataBaseException
	{
		Integer ID = ((org.dyndns.doujindb.db.cayenne.Convention)ref).getID();
		if(ID == null)
			return null;
		else
			return "E" + String.format("%08x", ID);
	}
	
	@Override
	public void doRecycle() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setRecycled(true);
		((DataBaseImpl)Core.Database)._recordRecycled(this);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setRecycled(false);
		((DataBaseImpl)Core.Database)._recordRestored(this);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Convention)ref).getRecycled();
	}

	@Override
	public void removeAll() throws DataBaseException
	{
		for(Book book : getBooks())
			removeBook(book);
	}
}
