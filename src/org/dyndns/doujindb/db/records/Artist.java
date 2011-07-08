package org.dyndns.doujindb.db.records;

import java.io.Serializable;
import java.util.Set;

import org.dyndns.doujindb.db.DouzRecord;
import org.dyndns.doujindb.db.containers.HasBook;
import org.dyndns.doujindb.db.containers.HasCircle;


public interface Artist extends DouzRecord, Serializable, HasBook, HasCircle
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
