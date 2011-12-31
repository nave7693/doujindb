package org.dyndns.doujindb.db;

import org.dyndns.doujindb.db.masks.MskArtist;
import org.dyndns.doujindb.db.masks.MskBook;
import org.dyndns.doujindb.db.masks.MskCircle;
import org.dyndns.doujindb.db.masks.MskContent;
import org.dyndns.doujindb.db.masks.MskConvention;
import org.dyndns.doujindb.db.masks.MskParody;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.db.records.Convention;
import org.dyndns.doujindb.db.records.Parody;

/**  
* DataBaseContext.java - DoujinDB database context.
* @author  nozomu
* @version 1.0
*/
public interface DataBaseContext
{
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
