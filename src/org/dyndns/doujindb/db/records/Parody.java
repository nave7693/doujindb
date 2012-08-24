package org.dyndns.doujindb.db.records;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Parody.java - Interface Parody.
* @author nozomu
* @version 1.0
*/
public interface Parody extends Record, CntBook
{
	public String getJapaneseName() throws DataBaseException;
	public String getTranslatedName() throws DataBaseException;
	public String getRomajiName() throws DataBaseException;
	public String getWeblink() throws DataBaseException;
	public void setJapaneseName(String japaneseName) throws DataBaseException;
	public void setTranslatedName(String translatedName) throws DataBaseException;
	public void setRomajiName(String romajiName) throws DataBaseException;
	public void setWeblink(String weblink) throws DataBaseException;
	public RecordSet<Book> getBooks() throws DataBaseException;
	public void addBook(Book book) throws DataBaseException;
	public void removeBook(Book book) throws DataBaseException;
	public void removeAll() throws DataBaseException;
}
