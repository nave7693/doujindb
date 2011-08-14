package org.dyndns.doujindb.db;

import org.dyndns.doujindb.db.records.*;

/**  
* DataBase.java - DoujinDB database driver.
* @author  nozomu
* @version 1.0
*/
public interface DataBase
{
	public String getConnection() throws DataBaseException;
	
	public void commit() throws DataBaseException;
	
	public void rollback() throws DataBaseException;
	
	public boolean getAutoCommit() throws DataBaseException;
	
	public void setAutoCommit(boolean autoCommit) throws DataBaseException;
	
	public Table<Book> getBooks() throws DataBaseException;

	public Table<Circle> getCircles() throws DataBaseException;

	public Table<Artist> getArtists() throws DataBaseException;

	public Table<Parody> getParodies() throws DataBaseException;

	public Table<Content> getContents() throws DataBaseException;

	public Table<Convention> getConventions() throws DataBaseException;
	
	public Table<Record> getDeleted() throws DataBaseException;

	public Table<Record> getShared() throws DataBaseException;

	public Table<Record> getUnchecked() throws DataBaseException;
	
	public Artist newArtist() throws DataBaseException;
	
	public Book newBook() throws DataBaseException;
	
	public Circle newCircle() throws DataBaseException;
	
	public Content newContent() throws DataBaseException;
	
	public Convention newConvention() throws DataBaseException;
	
	public Parody newParody() throws DataBaseException;
}
