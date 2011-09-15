package org.dyndns.doujindb.db.records;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Content.java - Interface Content.
* @author nozomu
* @version 1.0
*/
public interface Content extends Record, CntBook
{
	public String getTagName() throws DataBaseException;
	public String getInfo() throws DataBaseException;
	public void setTagName(String tagName) throws DataBaseException;
	public void setInfo(String info) throws DataBaseException;
	public RecordSet<Book> getBooks() throws DataBaseException;
	public void addBook(Book book) throws DataBaseException;
	public void removeBook(Book book) throws DataBaseException;
}
