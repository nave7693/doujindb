package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Set;

import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.BookContainer;

/**  
* Content.java - Interface Content.
* @author nozomu
* @version 1.0
*/
public interface Content extends Record, Serializable, BookContainer
{
	public String getTagName();
	public String getInfo();
	public void setTagName(String tagName);
	public void setInfo(String info);
	public Set<Book> getBooks();
}
