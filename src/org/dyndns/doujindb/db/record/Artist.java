package org.dyndns.doujindb.db.record;

import java.util.Set;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.container.*;

public interface Artist extends Record, BookContainer, CircleContainer, Comparable<Artist>
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
	public RecordSet<Circle> getCircles() throws DataBaseException;
	public void addBook(Book book) throws DataBaseException;
	public void addCircle(Circle circle) throws DataBaseException;
	public void removeBook(Book book) throws DataBaseException;
	public void removeCircle(Circle circle) throws DataBaseException;
	public Set<String> getAliases() throws DataBaseException;
	public void addAlias(String alias) throws DataBaseException;
	public void removeAlias(String alias) throws DataBaseException;
	public void removeAll() throws DataBaseException;
}
