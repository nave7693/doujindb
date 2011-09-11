package org.dyndns.doujindb.db.records;

import java.io.Serializable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Artist.java - Interface Artist.
* @author nozomu
* @version 1.0
*/
public interface Artist extends Record, Serializable, CntBook, CntCircle
{
	public String getJapaneseName();
	public String getTranslatedName();
	public String getRomanjiName();
	public String getWeblink();
	public void setJapaneseName(String japaneseName);
	public void setTranslatedName(String translatedName);
	public void setRomanjiName(String romanjiName);
	public void setWeblink(String weblink);
	public RecordSet<Book> getBooks();
	public RecordSet<Circle> getCircles();
	public void addBook(Book book);
	public void addCircle(Circle circle);
	public void removeBook(Book book);
	public void removeCircle(Circle circle);
}
