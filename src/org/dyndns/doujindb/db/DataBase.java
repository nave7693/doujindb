package org.dyndns.doujindb.db;

import java.io.Serializable;

import org.dyndns.doujindb.db.masks.*;
import org.dyndns.doujindb.db.records.*;

/**  
* DataBase.java - DoujinDB database driver.
* @author  nozomu
* @version 1.0
*/
public interface DataBase extends Serializable
{
	public String getConnection() throws DataBaseException;
	
	public void doCommit() throws DataBaseException;
	
	public void doRollback() throws DataBaseException;
	
	public RecordSet<Book> getBooks(MskBook mask) throws DataBaseException;

	public RecordSet<Circle> getCircles(MskCircle mask) throws DataBaseException;

	public RecordSet<Artist> getArtists(MskArtist mask) throws DataBaseException;

	public RecordSet<Parody> getParodies(MskParody mask) throws DataBaseException;

	public RecordSet<Content> getContents(MskContent mask) throws DataBaseException;

	public RecordSet<Convention> getConventions(MskConvention mask) throws DataBaseException;
	
	public RecordSet<Record> getRecycled() throws DataBaseException;
	
	public RecordSet<Record> getDeleted() throws DataBaseException;

	public RecordSet<Record> getModified() throws DataBaseException;
	
	public RecordSet<Record> getUncommitted() throws DataBaseException;
	
	public <T> T doInsert(Class<? extends Record> clazz) throws DataBaseException;
	
	public void doDelete(Record record) throws DataBaseException;
}
