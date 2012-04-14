package org.dyndns.doujindb.db.records;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Artist.java - Interface Artist.
* @author nozomu
* @version 1.0
*/
public interface Artist extends Record, CntBook, CntCircle
{
	public String getJapaneseName() throws DataBaseException;
	public String getTranslatedName() throws DataBaseException;
	public String getRomanjiName() throws DataBaseException;
	public String getWeblink() throws DataBaseException;
	public void setJapaneseName(String japaneseName) throws DataBaseException;
	public void setTranslatedName(String translatedName) throws DataBaseException;
	public void setRomanjiName(String romanjiName) throws DataBaseException;
	public void setWeblink(String weblink) throws DataBaseException;
	public RecordSet<Book> getBooks() throws DataBaseException;
	public RecordSet<Circle> getCircles() throws DataBaseException;
	public void addBook(Book book) throws DataBaseException;
	public void addCircle(Circle circle) throws DataBaseException;
	public void removeBook(Book book) throws DataBaseException;
	public void removeCircle(Circle circle) throws DataBaseException;
	public void removeAll() throws DataBaseException;
}
