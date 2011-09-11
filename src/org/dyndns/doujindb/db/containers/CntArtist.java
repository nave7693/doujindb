package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.RecordSet;
import org.dyndns.doujindb.db.records.*;

/**  
* CntArtist.java - Interface every item in the DB containing artist(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface CntArtist extends Remote
{
	public RecordSet<Artist> getArtists() throws RemoteException;
}
