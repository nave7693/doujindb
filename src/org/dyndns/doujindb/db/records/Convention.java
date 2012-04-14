package org.dyndns.doujindb.db.records;

import java.util.Set;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Convention.java - Interface Convention.
* @author nozomu
* @version 1.0
*/
public interface Convention extends Record, CntBook
{
	public String getTagName() throws DataBaseException;
	public String getInfo() throws DataBaseException;
	public String getWeblink() throws DataBaseException;
	public void setTagName(String tagName) throws DataBaseException;
	public void setInfo(String info) throws DataBaseException;
	public void setWeblink(String weblink) throws DataBaseException;
	public RecordSet<Book> getBooks() throws DataBaseException;
	public void addBook(Book book) throws DataBaseException;
	public void removeBook(Book book) throws DataBaseException;
	public Set<String> getAliases() throws DataBaseException;
	public void addAlias(String alias) throws DataBaseException;
	public void removeAlias(String alias) throws DataBaseException;
	public void removeAll() throws DataBaseException;
}
