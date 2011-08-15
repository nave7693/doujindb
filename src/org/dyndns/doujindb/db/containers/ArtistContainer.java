package org.dyndns.doujindb.db.containers;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import org.dyndns.doujindb.db.records.Artist;

/**  
* ArtistContainer.java - Interface every item in the DB containing artist(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface ArtistContainer extends Remote
{
	public Set<Artist> getArtists() throws RemoteException;
}
