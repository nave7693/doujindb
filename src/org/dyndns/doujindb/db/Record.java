package org.dyndns.doujindb.db;

/**  
* Record.java - DoujinDB database record.
* @author  nozomu
* @version 1.0
*/
public interface Record
{
	public String getID() throws DataBaseException;
	public void doRecycle() throws DataBaseException;
	public void doRestore() throws DataBaseException;
	public boolean isRecycled() throws DataBaseException;
}