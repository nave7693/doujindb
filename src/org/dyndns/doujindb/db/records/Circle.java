package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Set;

import org.dyndns.doujindb.db.DouzRecord;
import org.dyndns.doujindb.db.containers.HasArtist;
import org.dyndns.doujindb.db.containers.HasBook;


public interface Circle extends DouzRecord, Serializable, HasArtist, HasBook
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
