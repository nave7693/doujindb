package org.dyndns.doujindb.db.containers;

import java.util.Set;

import org.dyndns.doujindb.db.records.Book;


public interface HasBook
{
	public Set<Book> getBooks();
}
