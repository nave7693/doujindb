package org.dyndns.doujindb.dat;

interface IDataStore
{
	public abstract DataFile getMeta(String bookId) throws DataStoreException;
	public abstract DataFile getThumbnail(String bookId) throws DataStoreException;
	public abstract DataFile getFile(String bookId) throws DataStoreException;
}
