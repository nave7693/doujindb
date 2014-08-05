package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

public interface ArtistContainer
{
	public RecordSet<Artist> getArtists() throws DataBaseException;
	public void addArtist(Artist artist) throws DataBaseException;
	public void removeArtist(Artist artist) throws DataBaseException;
}
