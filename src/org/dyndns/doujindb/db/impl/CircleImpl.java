package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

final class CircleImpl extends RecordImpl implements Circle, Serializable//, Comparable<Circle>
{
	private static final long serialVersionUID = 0xFEED0001L;

	public CircleImpl(org.dyndns.doujindb.db.cayenne.Circle ref) throws DataBaseException
	{
		this.ref = ref;
	}

	@Override
	public synchronized String getJapaneseName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Circle)ref).getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setJapaneseName(japaneseName);
	}

	@Override
	public synchronized String getTranslatedName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Circle)ref).getTranslatedName();
	}

	@Override
	public synchronized void setTranslatedName(String translatedName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setTranslatedName(translatedName);
	}

	@Override
	public synchronized String getRomanjiName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Circle)ref).getRomanjiName();
	}

	@Override
	public synchronized void setRomanjiName(String romanjiName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setRomanjiName(romanjiName);
	}

	@Override
	public synchronized String getWeblink() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Circle)ref).getWeblink();
	}

	@Override
	public synchronized void setWeblink(String weblink) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setWeblink(weblink);
	}

	@Override
	public synchronized RecordSet<Artist> getArtists() throws DataBaseException
	{
		Set<Artist> set = new TreeSet<Artist>();
		Set<org.dyndns.doujindb.db.cayenne.Artist> result = ((org.dyndns.doujindb.db.cayenne.Circle)ref).getArtists();
		for(org.dyndns.doujindb.db.cayenne.Artist r : result)
			if(!r.getRecycled())
				set.add(new ArtistImpl(r));
		return new RecordSetImpl<Artist>(set);
	}

	@Override
	public synchronized RecordSet<Book> getBooks() throws DataBaseException
	{
		Set<Book> set = new TreeSet<Book>();
		/*
		 * Flattened Relationships n0:m0 : n1:m1 are considered read-only.
		 * There's a problem when updating data, these relations don't get updated, you have to rollback to see changes.
		 * Let's fix that.
		 * Set<org.dyndns.doujindb.db.cayenne.Book> result = ((org.dyndns.doujindb.db.cayenne.Circle)ref).getBooks();
		 */
		Set<org.dyndns.doujindb.db.cayenne.Book> result = new HashSet<org.dyndns.doujindb.db.cayenne.Book>();
		for(org.dyndns.doujindb.db.cayenne.Artist a : ((org.dyndns.doujindb.db.cayenne.Circle)ref).getArtists())
			if(!a.getRecycled())
				for(org.dyndns.doujindb.db.cayenne.Book b : a.getBooks())
					if(!b.getRecycled())
						result.add(b);
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
			set.add(new BookImpl(r));
		return new RecordSetImpl<Book>(set);
	}
	
	@Override
	public synchronized String toString()
	{
		return this.getJapaneseName();
		/*return japaneseName + 
			(romanjiName.equals("") ? "" : " ("+romanjiName+")") +
			(translatedName.equals("") ? "" : " ("+translatedName+")");*/
	}

	@Override
	public void addArtist(Artist artist) throws DataBaseException
	{
		if(getArtists().contains(artist))
			return;
		((org.dyndns.doujindb.db.cayenne.Circle)ref).addToArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.impl.ArtistImpl)artist).ref
		);
	}

	@Override
	public void removeArtist(Artist artist) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).removeFromArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.impl.ArtistImpl)artist).ref
		);
	}
	
	@Override
	public synchronized String getID() throws DataBaseException
	{
		Integer ID = ((org.dyndns.doujindb.db.cayenne.Circle)ref).getID();
		if(ID == null)
			return null;
		else
			return "C" + String.format("%08x", ID);
	}
	
	@Override
	public void doRecycle() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setRecycled(true);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setRecycled(false);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Circle)ref).getRecycled();
	}

	@Override
	public void removeAll() throws DataBaseException
	{
		Set<org.dyndns.doujindb.db.cayenne.Artist> result = ((org.dyndns.doujindb.db.cayenne.Circle)ref).getArtists();
		for(org.dyndns.doujindb.db.cayenne.Artist artist : result)
			((org.dyndns.doujindb.db.cayenne.Circle)ref).removeFromArtists(artist);
	}
}
