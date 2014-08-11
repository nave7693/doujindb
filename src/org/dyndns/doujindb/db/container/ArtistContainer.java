package org.dyndns.doujindb.db.container;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.record.*;

public interface ArtistContainer
{
	public RecordSet<Artist> getArtists() throws DataBaseException;
	public void addArtist(Artist artist) throws DataBaseException;
	public void removeArtist(Artist artist) throws DataBaseException;
}
