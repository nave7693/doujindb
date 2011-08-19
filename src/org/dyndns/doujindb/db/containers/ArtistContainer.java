package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

/**  
* ArtistContainer.java - Interface every item in the DB containing artist(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface ArtistContainer extends Remote
{
	public RecordSet<Artist> getArtists() throws RemoteException;
}
