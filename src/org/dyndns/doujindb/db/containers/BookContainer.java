package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* CntBook.java - Interface every item in the DB containing book(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface BookContainer
{
	public RecordSet<Book> getBooks() throws DataBaseException;
	public void addBook(Book book) throws DataBaseException;
	public void removeBook(Book book) throws DataBaseException;
}
