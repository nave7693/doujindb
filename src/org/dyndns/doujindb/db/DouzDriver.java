package org.dyndns.doujindb.db;

import org.dyndns.doujindb.db.records.*;

public interface DouzDriver
{
	public String getConnection() throws DatabaseException;
	
	public void commit() throws DatabaseException;
	
	public void rollback() throws DatabaseException;
	
	public boolean getAutoCommit() throws DatabaseException;
	
	public void setAutoCommit(boolean autoCommit) throws DatabaseException;
	
	public DouzTable<Book> getBooks() throws DatabaseException;

	public DouzTable<Circle> getCircles() throws DatabaseException;

	public DouzTable<Artist> getArtists() throws DatabaseException;

	public DouzTable<Parody> getParodies() throws DatabaseException;

	public DouzTable<Content> getContents() throws DatabaseException;

	public DouzTable<Convention> getConventions() throws DatabaseException;
	
	public DouzTable<DouzRecord> getDeleted() throws DatabaseException;

	public DouzTable<DouzRecord> getShared() throws DatabaseException;

	public DouzTable<DouzRecord> getUnchecked() throws DatabaseException;
	
	public Artist newArtist() throws DatabaseException;
	
	public Book newBook() throws DatabaseException;
	
	public Circle newCircle() throws DatabaseException;
	
	public Content newContent() throws DatabaseException;
	
	public Convention newConvention() throws DatabaseException;
	
	public Parody newParody() throws DatabaseException;
}
