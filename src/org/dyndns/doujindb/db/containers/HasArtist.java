package org.dyndns.doujindb.db.containers;

import java.util.Set;

import org.dyndns.doujindb.db.records.Artist;


public interface HasArtist
{
	public Set<Artist> getArtists();
}
