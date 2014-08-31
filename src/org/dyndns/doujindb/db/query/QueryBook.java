package org.dyndns.doujindb.db.query;

import java.util.*;

import org.dyndns.doujindb.db.record.Book;
import org.dyndns.doujindb.db.record.Content;

public final class QueryBook extends Query<Book>
{
	public Integer Id = null;
	public String JapaneseName = null;
	public String RomajiName = null;
	public String TranslatedName = null;
	public Book.Type Type = null;
	public Boolean Adult = null;
	public Set<Content> IncludeContents = new HashSet<Content>();
	public Set<Content> ExcludeContents = new HashSet<Content>();
}