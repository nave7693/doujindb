package org.dyndns.doujindb.dat;

interface IDataStore
{
	public abstract DataFile getMeta(String bookId) throws DataStoreException;
	public abstract DataFile getCover(String bookId) throws DataStoreException;
	public abstract DataFile getFile(String bookId) throws DataStoreException;
}
