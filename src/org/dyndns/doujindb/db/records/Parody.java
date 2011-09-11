package org.dyndns.doujindb.db.records;

import java.io.Serializable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Parody.java - Interface Parody.
* @author nozomu
* @version 1.0
*/
public interface Parody extends Record, Serializable, CntBook
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
	public void addBook(Book book);
	public void removeBook(Book book);
}
