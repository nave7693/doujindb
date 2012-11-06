package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.*;

final class BookImpl extends RecordImpl implements Book, Serializable//, Comparable<Book>
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	public BookImpl(org.dyndns.doujindb.db.cayenne.Book ref) throws DataBaseException
	{
		this.ref = ref;
	}

	@Override
	public synchronized String getJapaneseName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName) throws DataBaseException
	{
		if(getJapaneseName().equals(japaneseName))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setJapaneseName(japaneseName);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("japanese_name"));
	}

	@Override
	public synchronized String getTranslatedName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getTranslatedName();
	}
	
	@Override
	public synchronized void setTranslatedName(String translatedName) throws DataBaseException
	{
		if(getTranslatedName().equals(translatedName))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setTranslatedName(translatedName);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("translated_name"));
	}

	@Override
	public synchronized String getRomajiName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getRomajiName();
	}

	@Override
	public synchronized void setRomajiName(String romajiName) throws DataBaseException
	{
		if(getRomajiName().equals(romajiName))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRomajiName(romajiName);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("romaji_name"));
	}

	@Override
	public synchronized RecordSet<Artist> getArtists() throws DataBaseException
	{
		Set<Artist> set = new TreeSet<Artist>();
		Set<org.dyndns.doujindb.db.cayenne.Artist> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getArtists();
		for(org.dyndns.doujindb.db.cayenne.Artist r : result)
				set.add(new ArtistImpl(r));
		return new RecordSetImpl<Artist>(set);
	}

	@Override
	public synchronized RecordSet<Circle> getCircles() throws DataBaseException
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
	public synchronized RecordSet<Parody> getParodies() throws DataBaseException
	{
		Set<Parody> set = new TreeSet<Parody>();
		Set<org.dyndns.doujindb.db.cayenne.Parody> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getParodies();
		for(org.dyndns.doujindb.db.cayenne.Parody r : result)
			if(!r.getRecycled())
				set.add(new ParodyImpl(r));
		return new RecordSetImpl<Parody>(set);
	}

	@Override
	public synchronized Date getDate() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getPublished();
	}

	@Override
	public synchronized void setDate(Date released) throws DataBaseException
	{
		if(getDate().equals(released))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setPublished(released);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("released"));
	}

	@Override
	public synchronized Type getType() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getType();
	}

	@Override
	public synchronized void setType(Type type) throws DataBaseException
	{
		if(getType().equals(type))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setType(type);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("type"));
	}

	@Override
	public synchronized boolean isAdult() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getAdult();
	}

	@Override
	public synchronized void setAdult(boolean adult) throws DataBaseException
	{
		if(isAdult() == adult)
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setAdult(adult);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("adult"));
	}

	@Override
	public synchronized boolean isDecensored() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getDecensored();
	}

	@Override
	public synchronized void setDecensored(boolean decensored) throws DataBaseException
	{
		if(isDecensored() == decensored)
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setDecensored(decensored);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("decensored"));
	}

	@Override
	public synchronized boolean isTranslated() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getTranslated();
	}

	@Override
	public synchronized void setTranslated(boolean translated) throws DataBaseException
	{
		if(isTranslated() == translated)
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setTranslated(translated);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("translated"));
	}
	
	@Override
	public synchronized boolean isColored() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getColor();
	}

	@Override
	public synchronized void setColored(boolean colored) throws DataBaseException
	{
		if(isColored() == colored)
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setColor(colored);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("color"));
	}

	@Override
	public synchronized Rating getRating() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getRating();
	}

	@Override
	public synchronized void setRating(Rating rating) throws DataBaseException
	{
		if(getRating().equals(rating))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRating(rating);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("rating"));
	}
	
	@Override
	public synchronized RecordSet<Content> getContents() throws DataBaseException
	{
		Set<Content> set = new TreeSet<Content>();
		Set<org.dyndns.doujindb.db.cayenne.Content> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getContents();
		for(org.dyndns.doujindb.db.cayenne.Content r : result)
				set.add(new ContentImpl(r));
		return new RecordSetImpl<Content>(set);
	}

	@Override
	public synchronized int getPages() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getPages();
	}

	@Override
	public synchronized void setPages(int pages) throws DataBaseException
	{
		if(getPages() == pages)
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setPages(pages);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("pages"));
	}

	@Override
	public synchronized Convention getConvention() throws DataBaseException
	{
		if(((org.dyndns.doujindb.db.cayenne.Book)ref).getConventionof() == null)
			return null;
		return new ConventionImpl(((org.dyndns.doujindb.db.cayenne.Book)ref).getConventionof());
	}

	@Override
	public synchronized void setConvention(Convention convention) throws DataBaseException
	{
		if((getConvention() == null ? "NULL" : getConvention())
				.equals(
			(convention == null ? "NULL" : convention)))
			return;
		if(convention == null)
		{
			Convention conv = getConvention();
			((org.dyndns.doujindb.db.cayenne.Book)ref).setConventionof(null);
			((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(conv));
			((DataBaseImpl)Core.Database)._recordUpdated(conv, UpdateData.unlink(this));
		}
		else
		{
			Convention conv = getConvention();
			if(conv != null)
			{
				((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(conv));
				((DataBaseImpl)Core.Database)._recordUpdated(conv, UpdateData.unlink(this));
			}
			((org.dyndns.doujindb.db.cayenne.Book)ref).setConventionof((org.dyndns.doujindb.db.cayenne.Convention)((ConventionImpl)convention).ref);
			((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.link(convention));
			((DataBaseImpl)Core.Database)._recordUpdated(convention, UpdateData.link(this));
		}
	}

	@Override
	public synchronized String getInfo() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getInfo();
	}

	@Override
	public synchronized void setInfo(String info) throws DataBaseException
	{
		if(getInfo().equals(info))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).setInfo(info);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("info"));
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
	public void addArtist(Artist artist) throws DataBaseException
	{
		if(getArtists().contains(artist))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).addToArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.impl.ArtistImpl)artist).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.link(artist));
		((DataBaseImpl)Core.Database)._recordUpdated(artist, UpdateData.link(this));
	}

	@Override
	public void removeArtist(Artist artist) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.impl.ArtistImpl)artist).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(artist));
		((DataBaseImpl)Core.Database)._recordUpdated(artist, UpdateData.unlink(this));
	}

	@Override
	public void addContent(Content content) throws DataBaseException
	{
		if(getContents().contains(content))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).addToContents(
			(org.dyndns.doujindb.db.cayenne.Content)
			((org.dyndns.doujindb.db.impl.ContentImpl)content).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.link(content));
		((DataBaseImpl)Core.Database)._recordUpdated(content, UpdateData.link(this));
	}

	@Override
	public void addParody(Parody parody) throws DataBaseException
	{
		if(getParodies().contains(parody))
			return;
		((org.dyndns.doujindb.db.cayenne.Book)ref).addToParodies(
			(org.dyndns.doujindb.db.cayenne.Parody)
			((org.dyndns.doujindb.db.impl.ParodyImpl)parody).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.link(parody));
		((DataBaseImpl)Core.Database)._recordUpdated(parody, UpdateData.link(this));
	}

	@Override
	public void removeContent(Content content) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromContents(
				(org.dyndns.doujindb.db.cayenne.Content)
				((org.dyndns.doujindb.db.impl.ContentImpl)content).ref
			);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(content));
		((DataBaseImpl)Core.Database)._recordUpdated(content, UpdateData.unlink(this));
	}

	@Override
	public void removeParody(Parody parody) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromParodies(
				(org.dyndns.doujindb.db.cayenne.Parody)
				((org.dyndns.doujindb.db.impl.ParodyImpl)parody).ref
			);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(parody));
		((DataBaseImpl)Core.Database)._recordUpdated(parody, UpdateData.unlink(this));
	}
	
	@Override
	public synchronized String getID() throws DataBaseException
	{
		Integer ID = ((org.dyndns.doujindb.db.cayenne.Book)ref).getID();
		if(ID == null)
			return null;
		else
			return "B" + String.format("%08x", ID);
	}
	
	@Override
	public void doRecycle() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRecycled(true);
		((DataBaseImpl)Core.Database)._recordRecycled(this);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRecycled(false);
		((DataBaseImpl)Core.Database)._recordRestored(this);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getRecycled();
	}

	@Override
	public void removeAll() throws DataBaseException
	{
		for(Artist artist : getArtists())
		{
			for(Circle circle : artist.getCircles())
			{
				artist.removeCircle(circle);
				((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(circle));
				((DataBaseImpl)Core.Database)._recordUpdated(circle, UpdateData.unlink(this));
			}
			((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromArtists(
					(org.dyndns.doujindb.db.cayenne.Artist)
					((org.dyndns.doujindb.db.impl.ArtistImpl)artist).ref
				);
			((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(artist));
			((DataBaseImpl)Core.Database)._recordUpdated(artist, UpdateData.unlink(this));
		}
		for(Content content : getContents())
		{
			((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromContents(
					(org.dyndns.doujindb.db.cayenne.Content)
					((org.dyndns.doujindb.db.impl.ContentImpl)content).ref
				);
			((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(content));
			((DataBaseImpl)Core.Database)._recordUpdated(content, UpdateData.unlink(this));
		}
		for(Parody parody : getParodies())
		{
			((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromParodies(
					(org.dyndns.doujindb.db.cayenne.Parody)
					((org.dyndns.doujindb.db.impl.ParodyImpl)parody).ref
				);
			((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(parody));
			((DataBaseImpl)Core.Database)._recordUpdated(parody, UpdateData.unlink(this));
		}
		if(getConvention() != null)
		{
			((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(getConvention()));
			((DataBaseImpl)Core.Database)._recordUpdated(getConvention(), UpdateData.unlink(this));
		}
		setConvention(null);
	}
}