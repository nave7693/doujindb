package org.dyndns.doujindb.db.impl;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.DataBase;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.masks.*;
import org.dyndns.doujindb.db.records.*;

/**  
* RemoteDataBase.java - Remote database.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public
final class RemoteDataBase implements DataBase, Serializable
{
	private RMIDataBase remoteDataBase;

	public RemoteDataBase(RMIDataBase remoteDataBase) throws DataBaseException {
		this.remoteDataBase = remoteDataBase;
	}
	
	@Override
	public String getConnection() throws DataBaseException {
		try {
			return remoteDataBase.getConnection();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void doCommit() throws DataBaseException {
		try {
			remoteDataBase.doCommit();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void doRollback() throws DataBaseException {
		try {
			remoteDataBase.doRollback();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Book> getBooks(MskBook mask) throws DataBaseException {
		try {
			return remoteDataBase.getBooks(mask);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Circle> getCircles(MskCircle mask) throws DataBaseException {
		try {
			return remoteDataBase.getCircles(mask);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Artist> getArtists(MskArtist mask) throws DataBaseException {
		try {
			return remoteDataBase.getArtists(mask);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Parody> getParodies(MskParody mask) throws DataBaseException {
		try {
			return remoteDataBase.getParodies(mask);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Content> getContents(MskContent mask) throws DataBaseException {
		try {
			return remoteDataBase.getContents(mask);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Convention> getConventions(MskConvention mask) throws DataBaseException {
		try {
			return remoteDataBase.getConventions(mask);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Record> getRecycled() throws DataBaseException {
		try {
			return remoteDataBase.getRecycled();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Record> getDeleted() throws DataBaseException {
		try {
			return remoteDataBase.getDeleted();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Record> getModified() throws DataBaseException {
		try {
			return remoteDataBase.getModified();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public RecordSet<Record> getUncommitted() throws DataBaseException {
		try {
			return remoteDataBase.getUncommitted();
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public <T> T doInsert(Class<? extends Record> clazz) throws DataBaseException {
		try {
			return remoteDataBase.doInsert(clazz);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}

	@Override
	public void doDelete(Record record) throws DataBaseException {
		try {
			remoteDataBase.doDelete(record);
		} catch (RemoteException re) {
			throw new DataBaseException("RemoteException " + re);
		}
	}
}
