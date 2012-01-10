package org.dyndns.doujindb.db.query;

import org.dyndns.doujindb.db.records.Book;

/**  
* QueryBook.java - Used to query Book(s)
* @author nozomu
* @version 1.0
*/
public final class QueryBook
{
	public String JapaneseName = "%";
	public String RomanjiName = "%";
	public String TranslatedName = "%";
	public String Conventions = "%";
	public String Contents = "%";
	public Book.Type Type = Book.Type.不詳;
	public boolean Adult = false;
	public boolean Decensored = false;
	public boolean Translated = false;
	public boolean Colored = false;
}