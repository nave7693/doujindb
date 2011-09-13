package org.dyndns.doujindb.db.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.masks.*;
import org.dyndns.doujindb.db.records.*;

/**  
* RMIDataBaseImpl.java - RMI Implementation Database.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class RMIDataBaseImpl extends UnicastRemoteObject implements RMIDataBase
{
	private DataBase db;
	
	protected RMIDataBaseImpl(DataBase db) throws RemoteException
	{
		super(1099);
		this.db = db;
	}

	@Override
	public String getConnection() throws RemoteException {
		return db.getConnection();
	}	

	@Override
	public void doCommit() throws RemoteException {
		db.doCommit();
	}	

	@Override
	public void doRollback() throws RemoteException {
		db.doRollback();
	}	

	@Override
	public RecordSet<Book> getBooks(MskBook mask) throws RemoteException {
		return db.getBooks(mask);
	}

	@Override
	public RecordSet<Circle> getCircles(MskCircle mask) throws RemoteException {
		return db.getCircles(mask);
	}

	@Override
	public RecordSet<Artist> getArtists(MskArtist mask) throws RemoteException {
		return db.getArtists(mask);
	}

	@Override
	public RecordSet<Parody> getParodies(MskParody mask) throws RemoteException {
		return db.getParodies(mask);
	}

	@Override
	public RecordSet<Content> getContents(MskContent mask) throws RemoteException {
		return db.getContents(mask);
	}

	@Override
	public RecordSet<Convention> getConventions(MskConvention mask) throws RemoteException {
		return db.getConventions(mask);
	}

	@Override
	public RecordSet<Record> getRecycled() throws RemoteException {
		return db.getRecycled();
	}

	@Override
	public RecordSet<Record> getDeleted() throws RemoteException {
		return db.getDeleted();
	}

	@Override
	public RecordSet<Record> getModified() throws RemoteException {
		return db.getModified();
	}

	@Override
	public RecordSet<Record> getUncommitted() throws RemoteException {
		return db.getUncommitted();
	}

	@Override
	public <T> T doInsert(Class<? extends Record> clazz) throws RemoteException {
		return db.doInsert(clazz);
	}

	@Override
	public void doDelete(Record record) throws RemoteException {
		db.doDelete(record);
	}
}
