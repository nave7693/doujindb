package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Book")
final class BookImpl extends RecordImpl implements Book, Serializable//, Comparable<Book>
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	public BookImpl(org.dyndns.doujindb.db.cayenne.Book ref)
	{
		this.ref = ref;
		setRating(Rating.UNRATED);
		setType(Type.同人誌);
		setAdult(true);
		setColored(false);
		setDecensored(false);
		setTranslated(false);
		setPages(0);
		setDate(new Date());
	}

	@Override
	public synchronized String getJapaneseName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setJapaneseName(japaneseName);
	}

	@Override
	public synchronized String getTranslatedName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getTranslatedName();
	}
	
	@Override
	public synchronized void setTranslatedName(String translatedName)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setTranslatedName(translatedName);
	}

	@Override
	public synchronized String getRomanjiName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getRomanjiName();
	}

	@Override
	public synchronized void setRomanjiName(String romanjiName)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRomanjiName(romanjiName);
	}

	@Override
	public synchronized RecordSet<Artist> getArtists()
	{
		Set<Artist> set = new TreeSet<Artist>();
		Set<org.dyndns.doujindb.db.cayenne.Artist> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getArtists();
		for(org.dyndns.doujindb.db.cayenne.Artist r : result)
			set.add(new ArtistImpl(r));
		return new RecordSetImpl<Artist>(set);
	}

	@Override
	public synchronized RecordSet<Circle> getCircles()
	{
		Set<Circle> set = new TreeSet<Circle>();
		/*
		 * Flattened Relationships n0:m0 : n1:m1 are considered read-only.
		 * There's a problem when updating data, these relations don't get updated, you have to rollback to see changes.
		 * Let's fix that.
		 * Set<org.dyndns.doujindb.db.cayenne.Circle> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getCircles();
		 */
		Set<org.dyndns.doujindb.db.cayenne.Circle> result = new HashSet<org.dyndns.doujindb.db.cayenne.Circle>();
		for(org.dyndns.doujindb.db.cayenne.Artist a : ((org.dyndns.doujindb.db.cayenne.Book)ref).getArtists())
			for(org.dyndns.doujindb.db.cayenne.Circle c : a.getCircles())
				result.add(c);
		for(org.dyndns.doujindb.db.cayenne.Circle r : result)
			set.add(new CircleImpl(r));
		return new RecordSetImpl<Circle>(set);
	}

	@Override
	public synchronized RecordSet<Parody> getParodies()
	{
		Set<Parody> set = new TreeSet<Parody>();
		Set<org.dyndns.doujindb.db.cayenne.Parody> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getParodies();
		for(org.dyndns.doujindb.db.cayenne.Parody r : result)
			set.add(new ParodyImpl(r));
		return new RecordSetImpl<Parody>(set);
	}

	@Override
	public synchronized Date getDate()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getPublished();
	}

	@Override
	public synchronized void setDate(Date released)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setPublished(released);
	}

	@Override
	public synchronized Type getType()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getType();
	}

	@Override
	public synchronized void setType(Type type)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setType(type);
	}

	@Override
	public synchronized boolean isAdult()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getAdult();
	}

	@Override
	public synchronized void setAdult(boolean adult)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setAdult(adult);
	}

	@Override
	public synchronized boolean isDecensored()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getDecensored();
	}

	@Override
	public synchronized void setDecensored(boolean decensored)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setDecensored(decensored);
	}

	@Override
	public synchronized boolean isTranslated()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getTranslated();
	}

	@Override
	public synchronized void setTranslated(boolean translated)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setTranslated(translated);
	}
	
	@Override
	public synchronized boolean isColored()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getColor();
	}

	@Override
	public synchronized void setColored(boolean colored)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setColor(colored);
	}

	@Override
	public synchronized Rating getRating()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getRating();
	}

	@Override
	public synchronized void setRating(Rating rating)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRating(rating);
	}
	
	@Override
	public synchronized RecordSet<Content> getContents()
	{
		Set<Content> set = new TreeSet<Content>();
		Set<org.dyndns.doujindb.db.cayenne.Content> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getContents();
		for(org.dyndns.doujindb.db.cayenne.Content r : result)
			set.add(new ContentImpl(r));
		return new RecordSetImpl<Content>(set);
	}

	@Override
	public synchronized int getPages()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getPages();
	}

	@Override
	public synchronized void setPages(int pages)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setPages(pages);
	}

	@Override
	public synchronized Convention getConvention()
	{
		if(((org.dyndns.doujindb.db.cayenne.Book)ref).getConventionof() == null)
			return null;
		return new ConventionImpl(((org.dyndns.doujindb.db.cayenne.Book)ref).getConventionof());
	}

	@Override
	public synchronized void setConvention(Convention convention)
	{
		if(convention == null)
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setConventionof((org.dyndns.doujindb.db.cayenne.Convention)((ConventionImpl)convention).ref);
	}

	@Override
	public synchronized String getInfo()
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getInfo();
	}

	@Override
	public synchronized void setInfo(String info)
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setInfo(info);
	}
	
	@Override
	public synchronized String toString()
	{
		//return this.japaneseName;
		return "("+(getConvention()==null?"不詳":getConvention())+") " +
			"("+getType()+") " +
			"["+getCircles().toString().replaceAll("[\\[\\]]", "").replaceAll(",", "／") +
			"("+getArtists().toString().replaceAll("[\\[\\]]", "").replaceAll(",", "／")+")] " +
			""+getJapaneseName() + " " +
			"("+getParodies().toString().replaceAll("[\\[\\]]", "").replaceAll(",", "／")+")";
	}
	
	@Override
	public void addArtist(Artist artist) {
		if(getArtists().contains(artist))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).addToArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.impl.ArtistImpl)artist).ref
		);
	}

	@Override
	public void removeArtist(Artist artist) {
		((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.impl.ArtistImpl)artist).ref
		);
	}

	@Override
	public void addContent(Content content) {
		if(getContents().contains(content))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).addToContents(
			(org.dyndns.doujindb.db.cayenne.Content)
			((org.dyndns.doujindb.db.impl.ContentImpl)content).ref
		);
	}

	@Override
	public void addParody(Parody parody) {
		if(getParodies().contains(parody))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).addToParodies(
			(org.dyndns.doujindb.db.cayenne.Parody)
			((org.dyndns.doujindb.db.impl.ParodyImpl)parody).ref
		);
	}

	@Override
	public void removeContent(Content content) {
		((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromContents(
				(org.dyndns.doujindb.db.cayenne.Content)
				((org.dyndns.doujindb.db.impl.ContentImpl)content).ref
			);
	}

	@Override
	public void removeParody(Parody parody) {
		((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromParodies(
				(org.dyndns.doujindb.db.cayenne.Parody)
				((org.dyndns.doujindb.db.impl.ParodyImpl)parody).ref
			);
	}
	
	@Override
	public synchronized String getID()
	{
		return "B" + super.getID();
	}
}
