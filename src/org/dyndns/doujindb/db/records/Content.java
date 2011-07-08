package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Set;

import org.dyndns.doujindb.db.DouzRecord;
import org.dyndns.doujindb.db.containers.HasBook;


public interface Content extends DouzRecord, Serializable, HasBook
{
	public String getTagName();
	public String getInfo();
	public void setTagName(String tagName);
	public void setInfo(String info);
	public Set<Book> getBooks();
}
