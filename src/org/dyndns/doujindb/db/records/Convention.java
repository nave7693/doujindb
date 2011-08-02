package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Set;

import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.BookContainer;

/**  
* Convention.java - Interface Convention.
* @author nozomu
* @version 1.0
*/
public interface Convention extends Record, Serializable, BookContainer
{
	public String getTagName();
	public String getInfo();
	public String getWeblink();
	public void setTagName(String tagName);
	public void setInfo(String info);
	public void setWeblink(String weblink);
	public Set<Book> getBooks();
}
