package org.dyndns.doujindb.db;

import org.dyndns.doujindb.db.impl.*;
import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.records.*;

/**  
* DataBase.java - DoujinDB database instance.
* @author  nozomu
* @version 1.0
*/
public abstract class DataBase
{
	private static DataBase instance;
	
	static
	{
		instance = new DataBaseImpl();
	}
	
	public static DataBase getInstance()
	{
		return instance;
	}
	
	public abstract DataBaseContext getContext(String ID) throws DataBaseException;
	
	public abstract void doCommit() throws DataBaseException;

	public abstract void doRollback() throws DataBaseException;
	
	public abstract void doDelete(Record record) throws DataBaseException;
	
	public abstract RecordSet<Book> getBooks(QueryBook mask) throws DataBaseException;

	public abstract RecordSet<Circle> getCircles(QueryCircle mask) throws DataBaseException;

	public abstract RecordSet<Artist> getArtists(QueryArtist mask) throws DataBaseException;

	public abstract RecordSet<Parody> getParodies(QueryParody mask) throws DataBaseException;

	public abstract RecordSet<Content> getContents(QueryContent mask) throws DataBaseException;

	public abstract RecordSet<Convention> getConventions(QueryConvention mask) throws DataBaseException;
	
	public abstract RecordSet<Record> getRecycled() throws DataBaseException;

	public abstract RecordSet<Record> getDeleted() throws DataBaseException;

	public abstract RecordSet<Record> getModified() throws DataBaseException;

	public abstract RecordSet<Record> getUncommitted() throws DataBaseException;
	
	public abstract <T> T doInsert(Class<? extends Record> clazz) throws DataBaseException;
	
	public abstract void connect() throws DataBaseException;
	
	public abstract void disconnect() throws DataBaseException;
	
	public abstract boolean isConnected() throws DataBaseException;
	
	public abstract String getConnection() throws DataBaseException;

	public abstract boolean isAutocommit() throws DataBaseException;
}
