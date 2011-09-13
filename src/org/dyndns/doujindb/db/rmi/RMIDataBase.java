package org.dyndns.doujindb.db.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.masks.*;
import org.dyndns.doujindb.db.records.*;

/**  
* RMIDataBase.java - RMI Interface Database.
* @author  nozomu
* @version 1.0
*/
public interface RMIDataBase extends Remote
{
	public String getConnection() throws RemoteException;	
	public void doCommit() throws RemoteException;	
	public void doRollback() throws RemoteException;	
	public RecordSet<Book> getBooks(MskBook mask) throws RemoteException;
	public RecordSet<Circle> getCircles(MskCircle mask) throws RemoteException;
	public RecordSet<Artist> getArtists(MskArtist mask) throws RemoteException;
	public RecordSet<Parody> getParodies(MskParody mask) throws RemoteException;
	public RecordSet<Content> getContents(MskContent mask) throws RemoteException;
	public RecordSet<Convention> getConventions(MskConvention mask) throws RemoteException;
	public RecordSet<Record> getRecycled() throws RemoteException;
	public RecordSet<Record> getDeleted() throws RemoteException;
	public RecordSet<Record> getModified() throws RemoteException;
	public RecordSet<Record> getUncommitted() throws RemoteException;
	public <T> T doInsert(Class<? extends Record> clazz) throws RemoteException;
	public void doDelete(Record record) throws RemoteException;
}
