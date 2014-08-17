package org.dyndns.doujindb.db.record;

import java.util.Set;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.container.*;

public interface Content extends Record, BookContainer, Comparable<Content>
{
	public enum Namespace implements org.apache.cayenne.ExtendedEnumeration
	{
		Misc(0), Language(1), Male(2), Female(3), Character(4);

		private Integer value;

		private Namespace(Integer value) { this.value = value; }
		
		public Integer getDatabaseValue() { return value; }
	}
	
	public String getTagName() throws DataBaseException;
	public String getInfo() throws DataBaseException;
	public Namespace getNamespace() throws DataBaseException;
	public void setTagName(String tagName) throws DataBaseException;
	public void setInfo(String info) throws DataBaseException;
	public void setNamespace(Namespace namespace) throws DataBaseException;
	public RecordSet<Book> getBooks() throws DataBaseException;
	public void addBook(Book book) throws DataBaseException;
	public void removeBook(Book book) throws DataBaseException;
	public Set<String> getAliases() throws DataBaseException;
	public void addAlias(String alias) throws DataBaseException;
	public void removeAlias(String alias) throws DataBaseException;
	public void removeAll() throws DataBaseException;
}
