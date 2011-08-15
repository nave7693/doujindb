package org.dyndns.doujindb.db;

import java.io.Serializable;
import java.rmi.*;

import org.dyndns.doujindb.db.records.*;

/**  
* DataBase.java - DoujinDB database driver.
* @author  nozomu
* @version 1.0
*/
public interface DataBase extends Remote, Serializable
{
	public String getConnection() throws DataBaseException, RemoteException;
	
	public void commit() throws DataBaseException, RemoteException;
	
	public void rollback() throws DataBaseException, RemoteException;
	
	public boolean getAutoCommit() throws DataBaseException, RemoteException;
	
	public void setAutoCommit(boolean autoCommit) throws DataBaseException, RemoteException;
	
	public Table<Book> getBooks() throws DataBaseException, RemoteException;

	public Table<Circle> getCircles() throws DataBaseException, RemoteException;

	public Table<Artist> getArtists() throws DataBaseException, RemoteException;

	public Table<Parody> getParodies() throws DataBaseException, RemoteException;

	public Table<Content> getContents() throws DataBaseException, RemoteException;

	public Table<Convention> getConventions() throws DataBaseException, RemoteException;
	
	public Table<Record> getDeleted() throws DataBaseException, RemoteException;

	public Table<Record> getShared() throws DataBaseException, RemoteException;

	public Table<Record> getUnchecked() throws DataBaseException, RemoteException;
	
	public Artist newArtist() throws DataBaseException, RemoteException;
	
	public Book newBook() throws DataBaseException, RemoteException;
	
	public Circle newCircle() throws DataBaseException, RemoteException;
	
	public Content newContent() throws DataBaseException, RemoteException;
	
	public Convention newConvention() throws DataBaseException, RemoteException;
	
	public Parody newParody() throws DataBaseException, RemoteException;
}
