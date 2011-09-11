package org.dyndns.doujindb.db.records;

import java.io.Serializable;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.containers.*;

/**  
* Circle.java - Interface Circle.
* @author nozomu
* @version 1.0
*/
public interface Circle extends Record, Serializable, CntArtist, CntBook
{
	public String getJapaneseName();
	public String getTranslatedName();
	public String getRomanjiName();
	public String getWeblink();
	public void setJapaneseName(String japaneseName);
	public void setTranslatedName(String translatedName);
	public void setRomanjiName(String romanjiName);
	public void setWeblink(String weblink);
	public RecordSet<Artist> getArtists();
	public RecordSet<Book> getBooks();
	public void addArtist(Artist artist);
	public void removeArtist(Artist artist);
}
