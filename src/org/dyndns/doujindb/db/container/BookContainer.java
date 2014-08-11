package org.dyndns.doujindb.db.container;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.record.*;

public interface BookContainer
{
	public RecordSet<Book> getBooks() throws DataBaseException;
	public void addBook(Book book) throws DataBaseException;
	public void removeBook(Book book) throws DataBaseException;
}
