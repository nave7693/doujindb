package org.dyndns.doujindb.db.records;

import java.io.Serializable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Convention.java - Interface Convention.
* @author nozomu
* @version 1.0
*/
public interface Convention extends Record, Serializable, CntBook
{
	public String getTagName();
	public String getInfo();
	public String getWeblink();
	public void setTagName(String tagName);
	public void setInfo(String info);
	public void setWeblink(String weblink);
	public RecordSet<Book> getBooks();
	public void addBook(Book book);
	public void removeBook(Book book);
}
