package org.dyndns.doujindb.db;

import org.dyndns.doujindb.db.records.*;

/**  
* Driver.java - DoujinDB database driver.
* @author  nozomu
* @version 1.0
*/
public interface Driver
{
	public String getConnection() throws DatabaseException;
	
	public void commit() throws DatabaseException;
	
	public void rollback() throws DatabaseException;
	
	public boolean getAutoCommit() throws DatabaseException;
	
	public void setAutoCommit(boolean autoCommit) throws DatabaseException;
	
	public Table<Book> getBooks() throws DatabaseException;

	public Table<Circle> getCircles() throws DatabaseException;

	public Table<Artist> getArtists() throws DatabaseException;

	public Table<Parody> getParodies() throws DatabaseException;

	public Table<Content> getContents() throws DatabaseException;

	public Table<Convention> getConventions() throws DatabaseException;
	
	public Table<Record> getDeleted() throws DatabaseException;

	public Table<Record> getShared() throws DatabaseException;

	public Table<Record> getUnchecked() throws DatabaseException;
	
	public Artist newArtist() throws DatabaseException;
	
	public Book newBook() throws DatabaseException;
	
	public Circle newCircle() throws DatabaseException;
	
	public Content newContent() throws DatabaseException;
	
	public Convention newConvention() throws DatabaseException;
	
	public Parody newParody() throws DatabaseException;
}
