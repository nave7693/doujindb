package org.dyndns.doujindb.dat;

interface IDataStore
{
	public abstract DataFile.MetaData getMetadata(Integer bookId) throws DataStoreException;
	public abstract DataFile getThumbnail(Integer bookId) throws DataStoreException;
	public abstract DataFile getBanner(Integer circleId) throws DataStoreException;
	public abstract DataFile getStore(Integer bookId) throws DataStoreException;
}
