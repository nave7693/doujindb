package org.dyndns.doujindb.db.query;

import org.dyndns.doujindb.db.records.Book;

/**  
* QueryBook.java - Used to query Book(s)
* @author nozomu
* @version 1.0
*/
public final class QueryBook extends Query<Book>
{
	public String ID = null;
	public String JapaneseName = null;
	public String RomajiName = null;
	public String TranslatedName = null;
	public Book.Type Type = null;
	public Boolean Adult = null;
	public Boolean Decensored = null;
	public Boolean Translated = null;
	public Boolean Colored = null;
}