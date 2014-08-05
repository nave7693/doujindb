package org.dyndns.doujindb.db;

public interface Record
{
	public Integer getId() throws DataBaseException;
	public void doRecycle() throws DataBaseException;
	public void doRestore() throws DataBaseException;
	public boolean isRecycled() throws DataBaseException;
}