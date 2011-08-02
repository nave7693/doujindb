package org.dyndns.doujindb.db;

/**  
* Table.java - DoujinDB database table.
* @author  nozomu
* @version 1.0
*/
public interface Table<T extends Record> extends Iterable<T>
{
	public void insert(T row) throws DatabaseException;
	public void delete(T row) throws DatabaseException;
	public boolean contains(T row) throws DatabaseException;
	public long count() throws DatabaseException;
}