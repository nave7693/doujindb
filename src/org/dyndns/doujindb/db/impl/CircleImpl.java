package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Circle")
final class CircleImpl extends RecordImpl implements Circle, Serializable//, Comparable<Circle>
{
	private static final long serialVersionUID = 0xFEED0001L;

	public CircleImpl(org.dyndns.doujindb.db.cayenne.Circle ref)
	{
		this.ref = ref;
	}

	@Override
	public synchronized String getJapaneseName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Circle)ref).getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName)
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setJapaneseName(japaneseName);
	}

	@Override
	public synchronized String getTranslatedName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Circle)ref).getTranslatedName();
	}

	@Override
	public synchronized void setTranslatedName(String translatedName)
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setTranslatedName(translatedName);
	}

	@Override
	public synchronized String getRomanjiName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Circle)ref).getRomanjiName();
	}

	@Override
	public synchronized void setRomanjiName(String romanjiName)
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setRomanjiName(romanjiName);
	}

	@Override
	public synchronized String getWeblink()
	{
		return ((org.dyndns.doujindb.db.cayenne.Circle)ref).getWeblink();
	}

	@Override
	public synchronized void setWeblink(String weblink)
	{
		((org.dyndns.doujindb.db.cayenne.Circle)ref).setWeblink(weblink);
	}

	@Override
	public synchronized RecordSet<Artist> getArtists()
	{
		Set<Artist> set = new TreeSet<Artist>();
		Set<org.dyndns.doujindb.db.cayenne.Artist> result = ((org.dyndns.doujindb.db.cayenne.Circle)ref).getArtists();
		for(org.dyndns.doujindb.db.cayenne.Artist r : result)
			set.add(new ArtistImpl(r));
		return new RecordSetImpl<Artist>(set);
	}

	@Override
	public synchronized RecordSet<Book> getBooks()
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
			for(org.dyndns.doujindb.db.cayenne.Book b : a.getBooks())
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
	public void addArtist(Artist artist) {
		if(getArtists().contains(artist))
			return;
		((org.dyndns.doujindb.db.cayenne.Circle)ref).addToArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.impl.ArtistImpl)artist).ref
		);
	}

	@Override
	public void removeArtist(Artist artist) {
		((org.dyndns.doujindb.db.cayenne.Circle)ref).removeFromArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.impl.ArtistImpl)artist).ref
		);
	}
	
	@Override
	public synchronized String getID()
	{
		return "C" + super.getID();
	}
}
