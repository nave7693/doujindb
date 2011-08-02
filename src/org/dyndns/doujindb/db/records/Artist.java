package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Set;

import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.BookContainer;
import org.dyndns.doujindb.db.containers.CircleContainer;

/**  
* Artist.java - Interface Artist.
* @author nozomu
* @version 1.0
*/
public interface Artist extends Record, Serializable, BookContainer, CircleContainer
{
	public String getJapaneseName();
	public String getTranslatedName();
	public String getRomanjiName();
	public String getWeblink();
	public void setJapaneseName(String japaneseName);
	public void setTranslatedName(String translatedName);
	public void setRomanjiName(String romanjiName);
	public void setWeblink(String weblink);
	public Set<Book> getBooks();
	public Set<Circle> getCircles();
}
