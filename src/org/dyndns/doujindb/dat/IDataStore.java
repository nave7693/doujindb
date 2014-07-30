package org.dyndns.doujindb.dat;

interface IDataStore
{
	public abstract DataFile.MetaData getMetadata(String bookId) throws DataStoreException;
	public abstract DataFile getThumbnail(String bookId) throws DataStoreException;
	public abstract DataFile getBanner(String circleId) throws DataStoreException;
	public abstract DataFile getStore(String bookId) throws DataStoreException;
}
