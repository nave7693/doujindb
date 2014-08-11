package org.dyndns.doujindb.db;

import org.dyndns.doujindb.db.query.*;
import org.dyndns.doujindb.db.record.*;
import org.dyndns.doujindb.db.cayenne.ContentAlias;
import org.dyndns.doujindb.db.cayenne.ConventionAlias;

public abstract class IDataBase
{
	public abstract void doCommit() throws DataBaseException;
	public abstract void doRollback() throws DataBaseException;
	public abstract void doDelete(Record record) throws DataBaseException;
	public abstract RecordSet<Book> getBooks(QueryBook query) throws DataBaseException;
	public abstract RecordSet<Circle> getCircles(QueryCircle query) throws DataBaseException;
	public abstract RecordSet<Artist> getArtists(QueryArtist query) throws DataBaseException;
	public abstract RecordSet<Parody> getParodies(QueryParody query) throws DataBaseException;
	public abstract RecordSet<Content> getContents(QueryContent query) throws DataBaseException;
	public abstract RecordSet<Convention> getConventions(QueryConvention query) throws DataBaseException;
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
	protected abstract Artist newArtist() throws DataBaseException;
	protected abstract Book newBook() throws DataBaseException;
	protected abstract Circle newCircle() throws DataBaseException;
	protected abstract Content newContent() throws DataBaseException;
	protected abstract ContentAlias newContentAlias() throws DataBaseException;
	protected abstract Convention newConvention() throws DataBaseException;
	protected abstract ConventionAlias newConventionAlias() throws DataBaseException;
	protected abstract Parody newParody() throws DataBaseException;
	protected abstract void deleteObject(Object o) throws DataBaseException;
}
