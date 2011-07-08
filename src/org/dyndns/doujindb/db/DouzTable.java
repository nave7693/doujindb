package org.dyndns.doujindb.db;

public interface DouzTable<T extends DouzRecord> extends Iterable<T>
{
	public void insert(T row) throws DatabaseException;
	public void delete(T row) throws DatabaseException;
	public boolean contains(T row) throws DatabaseException;
	public long count() throws DatabaseException;
}