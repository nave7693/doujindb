package org.dyndns.doujindb.db.query;

import java.util.*;

import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Content;

public final class QueryBook extends Query<Book>
{
	public Integer Id = null;
	public String JapaneseName = null;
	public String RomajiName = null;
	public String TranslatedName = null;
	public Book.Type Type = null;
	public Boolean Adult = null;
	public Boolean Decensored = null;
	public Boolean Translated = null;
	public Boolean Colored = null;
	public Set<Content> Contents = new HashSet<Content>();
}