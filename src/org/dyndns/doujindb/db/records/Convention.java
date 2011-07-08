package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Set;

import org.dyndns.doujindb.db.DouzRecord;
import org.dyndns.doujindb.db.containers.HasBook;


public interface Convention extends DouzRecord, Serializable, HasBook
{
	public String getTagName();
	public String getInfo();
	public String getWeblink();
	public void setTagName(String tagName);
	public void setInfo(String info);
	public void setWeblink(String weblink);
	public Set<Book> getBooks();
}
