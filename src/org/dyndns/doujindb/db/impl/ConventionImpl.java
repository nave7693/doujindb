package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Convention")
final class ConventionImpl extends RecordImpl implements Convention, Serializable//, Comparable<Convention>
{
	private static final long serialVersionUID = 0xFEED0001L;

	public ConventionImpl(org.dyndns.doujindb.db.cayenne.Convention ref)
	{
		this.ref = ref;
		doRestore();
	}

	@Override
	public synchronized String getTagName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Convention)ref).getTagName();
	}

	@Override
	public synchronized void setTagName(String tagName)
	{
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setTagName(tagName);
	}

	@Override
	public synchronized String getInfo()
	{
		return ((org.dyndns.doujindb.db.cayenne.Convention)ref).getInfo();
	}

	@Override
	public synchronized void setInfo(String info)
	{
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setInfo(info);
	}	
	
	@Override
	public synchronized String getWeblink()
	{
		return ((org.dyndns.doujindb.db.cayenne.Convention)ref).getWeblink();
	}

	@Override
	public synchronized void setWeblink(String weblink)
	{
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setWeblink(weblink);
	}

	@Override
	public synchronized RecordSet<Book> getBooks()
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ((org.dyndns.doujindb.db.cayenne.Convention)ref).getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
			if(!r.getRecycled())
				set.add(new BookImpl(r));
		return new RecordSetImpl<Book>(set);
	}

	@Override
	public synchronized String toString()
	{
		return this.getTagName();
	}
	
	@Override
	public void addBook(Book book) {
		if(getBooks().contains(book))
			return;
		((org.dyndns.doujindb.db.cayenne.Convention)ref).addToBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
	}

	@Override
	public void removeBook(Book book) {
		((org.dyndns.doujindb.db.cayenne.Convention)ref).removeFromBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
	}
	
	@Override
	public synchronized String getID()
	{
		return "E" + super.getID();
	}
	
	@Override
	public void doRecycle()
	{
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setRecycled(true);
	}

	@Override
	public void doRestore()
	{
		((org.dyndns.doujindb.db.cayenne.Convention)ref).setRecycled(false);
	}

	@Override
	boolean isRecycled() {
		return ((org.dyndns.doujindb.db.cayenne.Convention)ref).getRecycled();
	}
}
