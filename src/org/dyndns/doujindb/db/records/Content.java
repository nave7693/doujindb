package org.dyndns.doujindb.db.records;

import java.io.Serializable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Content.java - Interface Content.
* @author nozomu
* @version 1.0
*/
public interface Content extends Record, Serializable, CntBook
{
	public String getTagName();
	public String getInfo();
	public void setTagName(String tagName);
	public void setInfo(String info);
	public RecordSet<Book> getBooks();
	public void addBook(Book book);
	public void removeBook(Book book);
}
