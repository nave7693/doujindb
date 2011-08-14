package org.dyndns.doujindb.db;

/**  
* Table.java - DoujinDB database table.
* @author  nozomu
* @version 1.0
*/
public interface Table<T extends Record> extends Iterable<T>
{
	public void insert(T row) throws DataBaseException;
	public void delete(T row) throws DataBaseException;
	public boolean contains(T row) throws DataBaseException;
	public long count() throws DataBaseException;
}