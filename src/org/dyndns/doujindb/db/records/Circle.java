package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Set;

import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.ArtistContainer;
import org.dyndns.doujindb.db.containers.BookContainer;

/**  
* Circle.java - Interface Circle.
* @author nozomu
* @version 1.0
*/
public interface Circle extends Record, Serializable, ArtistContainer, BookContainer
{
	public String getJapaneseName();
	public String getTranslatedName();
	public String getRomanjiName();
	public String getWeblink();
	public void setJapaneseName(String japaneseName);
	public void setTranslatedName(String translatedName);
	public void setRomanjiName(String romanjiName);
	public void setWeblink(String weblink);
	public Set<Artist> getArtists();
	public Set<Book> getBooks();
}
