package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Content")
final class ContentImpl extends RecordImpl implements Content, Serializable//, Comparable<Content>
{
	private static final long serialVersionUID = 0xFEED0001L;

	public ContentImpl(org.dyndns.doujindb.db.cayenne.Content ref)
	{
		this.ref = ref;
	}

	@Override
	public synchronized String getTagName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Content)ref).getTagName();
	}

	@Override
	public synchronized void setTagName(String tagName)
	{
		((org.dyndns.doujindb.db.cayenne.Content)ref).setTagName(tagName);
	}

	@Override
	public synchronized String getInfo()
	{
		return ((org.dyndns.doujindb.db.cayenne.Content)ref).getInfo();
	}

	@Override
	public synchronized void setInfo(String info)
	{
		((org.dyndns.doujindb.db.cayenne.Content)ref).setInfo(info);
	}
	
	@Override
	public synchronized RecordSet<Book> getBooks()
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ((org.dyndns.doujindb.db.cayenne.Content)ref).getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
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
		((org.dyndns.doujindb.db.cayenne.Content)ref).addToBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
	}

	@Override
	public void removeBook(Book book) {
		((org.dyndns.doujindb.db.cayenne.Content)ref).removeFromBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
	}
}
