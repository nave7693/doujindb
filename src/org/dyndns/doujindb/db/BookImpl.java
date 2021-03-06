package org.dyndns.doujindb.db;

import java.util.*;

import org.apache.cayenne.CayenneDataObject;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.record.*;

@SuppressWarnings("serial")
final class BookImpl extends RecordImpl implements Book
{
	org.dyndns.doujindb.db.cayenne.Book ref;
	
	public BookImpl(org.dyndns.doujindb.db.cayenne.Book ref) throws DataBaseException
	{
		this.ref = ref;
	}

	@Override
	public synchronized String getJapaneseName() throws DataBaseException
	{
		return ref.getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName) throws DataBaseException
	{
		if(getJapaneseName().equals(japaneseName))
			return;
		ref.setJapaneseName(japaneseName);
		DataBase.fireRecordUpdated(this, UpdateData.property("japanese_name"));
	}

	@Override
	public synchronized String getTranslatedName() throws DataBaseException
	{
		return ref.getTranslatedName();
	}
	
	@Override
	public synchronized void setTranslatedName(String translatedName) throws DataBaseException
	{
		if(getTranslatedName().equals(translatedName))
			return;
		ref.setTranslatedName(translatedName);
		DataBase.fireRecordUpdated(this, UpdateData.property("translated_name"));
	}

	@Override
	public synchronized String getRomajiName() throws DataBaseException
	{
		return ref.getRomajiName();
	}

	@Override
	public synchronized void setRomajiName(String romajiName) throws DataBaseException
	{
		if(getRomajiName().equals(romajiName))
			return;
		ref.setRomajiName(romajiName);
		DataBase.fireRecordUpdated(this, UpdateData.property("romaji_name"));
	}

	@Override
	public synchronized RecordSet<Artist> getArtists() throws DataBaseException
	{
		Set<Artist> set = new TreeSet<Artist>();
		Set<org.dyndns.doujindb.db.cayenne.Artist> result = ref.getArtists();
		for(org.dyndns.doujindb.db.cayenne.Artist r : result)
				set.add(new ArtistImpl(r));
		return new RecordSetImpl<Artist>(set);
	}

	@Override
	public synchronized RecordSet<Circle> getCircles() throws DataBaseException
	{
		Set<Circle> set = new TreeSet<Circle>();
		Set<org.dyndns.doujindb.db.cayenne.Circle> result = ref.getCircles();
		for(org.dyndns.doujindb.db.cayenne.Circle r : result)
				set.add(new CircleImpl(r));
		return new RecordSetImpl<Circle>(set);
	}

	@Override
	public synchronized RecordSet<Parody> getParodies() throws DataBaseException
	{
		Set<Parody> set = new TreeSet<Parody>();
		Set<org.dyndns.doujindb.db.cayenne.Parody> result = ref.getParodies();
		for(org.dyndns.doujindb.db.cayenne.Parody r : result)
			if(!r.getRecycled())
				set.add(new ParodyImpl(r));
		return new RecordSetImpl<Parody>(set);
	}

	@Override
	public synchronized Date getDate() throws DataBaseException
	{
		return ref.getPublished();
	}

	@Override
	public synchronized void setDate(Date released) throws DataBaseException
	{
		if(getDate().equals(released))
			return;
		ref.setPublished(released);
		DataBase.fireRecordUpdated(this, UpdateData.property("released"));
	}

	@Override
	public synchronized Type getType() throws DataBaseException
	{
		return ref.getType();
	}

	@Override
	public synchronized void setType(Type type) throws DataBaseException
	{
		if(getType().equals(type))
			return;
		ref.setType(type);
		DataBase.fireRecordUpdated(this, UpdateData.property("type"));
	}

	@Override
	public synchronized boolean isAdult() throws DataBaseException
	{
		return ref.getAdult();
	}

	@Override
	public synchronized void setAdult(boolean adult) throws DataBaseException
	{
		if(isAdult() == adult)
			return;
		ref.setAdult(adult);
		DataBase.fireRecordUpdated(this, UpdateData.property("adult"));
	}

	@Override
	public synchronized Rating getRating() throws DataBaseException
	{
		return ref.getRating();
	}

	@Override
	public synchronized void setRating(Rating rating) throws DataBaseException
	{
		if(getRating().equals(rating))
			return;
		ref.setRating(rating);
		DataBase.fireRecordUpdated(this, UpdateData.property("rating"));
	}
	
	@Override
	public synchronized RecordSet<Content> getContents() throws DataBaseException
	{
		Set<Content> set = new TreeSet<Content>();
		Set<org.dyndns.doujindb.db.cayenne.Content> result = ref.getContents();
		for(org.dyndns.doujindb.db.cayenne.Content r : result)
				set.add(new ContentImpl(r));
		return new RecordSetImpl<Content>(set);
	}

	@Override
	public synchronized int getPages() throws DataBaseException
	{
		return ref.getPages();
	}

	@Override
	public synchronized void setPages(int pages) throws DataBaseException
	{
		if(getPages() == pages)
			return;
		ref.setPages(pages);
		DataBase.fireRecordUpdated(this, UpdateData.property("pages"));
	}

	@Override
	public synchronized Convention getConvention() throws DataBaseException
	{
		if(ref.getConventionof() == null)
			return null;
		return new ConventionImpl(ref.getConventionof());
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
			ref.setConventionof(null);
			DataBase.fireRecordUpdated(this, UpdateData.unlink(conv));
			DataBase.fireRecordUpdated(conv, UpdateData.unlink(this));
		}
		else
		{
			Convention conv = getConvention();
			if(conv != null)
			{
				DataBase.fireRecordUpdated(this, UpdateData.unlink(conv));
				DataBase.fireRecordUpdated(conv, UpdateData.unlink(this));
			}
			ref.setConventionof((org.dyndns.doujindb.db.cayenne.Convention)((ConventionImpl)convention).ref);
			DataBase.fireRecordUpdated(this, UpdateData.link(convention));
			DataBase.fireRecordUpdated(convention, UpdateData.link(this));
		}
	}

	@Override
	public synchronized String getInfo() throws DataBaseException
	{
		return ref.getInfo();
	}

	@Override
	public synchronized void setInfo(String info) throws DataBaseException
	{
		if(getInfo().equals(info))
			return;
		ref.setInfo(info);
		DataBase.fireRecordUpdated(this, UpdateData.property("info"));
	}
	
	@Override
	public synchronized Set<String> getAliases() throws DataBaseException
	{
		Set<String> set = new TreeSet<String>();
		Set<org.dyndns.doujindb.db.cayenne.BookAlias> result = ref.getAliases();
		for(org.dyndns.doujindb.db.cayenne.BookAlias r : result)
			set.add(r.getName());
		return set;
	}
	
	@Override
	public synchronized String toString()
	{
		String translation;
		if(!(translation = getTranslatedName()).equals(""))
			return this.getJapaneseName() + " (" + translation + ")";
		else
			return this.getJapaneseName();	}
	
	@Override
	public void addArtist(Artist artist) throws DataBaseException
	{
		ref.addToArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.ArtistImpl)artist).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.link(artist));
		DataBase.fireRecordUpdated(artist, UpdateData.link(this));
	}

	@Override
	public void removeArtist(Artist artist) throws DataBaseException
	{
		ref.removeFromArtists(
			(org.dyndns.doujindb.db.cayenne.Artist)
			((org.dyndns.doujindb.db.ArtistImpl)artist).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.unlink(artist));
		DataBase.fireRecordUpdated(artist, UpdateData.unlink(this));
	}
	
	@Override
	public void addCircle(Circle circle) throws DataBaseException
	{
		ref.addToCircles(
			(org.dyndns.doujindb.db.cayenne.Circle)
			((org.dyndns.doujindb.db.CircleImpl)circle).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.link(circle));
		DataBase.fireRecordUpdated(circle, UpdateData.link(this));
	}

	@Override
	public void removeCircle(Circle circle) throws DataBaseException
	{
		ref.removeFromCircles(
			(org.dyndns.doujindb.db.cayenne.Circle)
			((org.dyndns.doujindb.db.CircleImpl)circle).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.unlink(circle));
		DataBase.fireRecordUpdated(circle, UpdateData.unlink(this));
	}

	@Override
	public void addContent(Content content) throws DataBaseException
	{
		ref.addToContents(
			(org.dyndns.doujindb.db.cayenne.Content)
			((org.dyndns.doujindb.db.ContentImpl)content).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.link(content));
		DataBase.fireRecordUpdated(content, UpdateData.link(this));
	}

	@Override
	public void addParody(Parody parody) throws DataBaseException
	{
		ref.addToParodies(
			(org.dyndns.doujindb.db.cayenne.Parody)
			((org.dyndns.doujindb.db.ParodyImpl)parody).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.link(parody));
		DataBase.fireRecordUpdated(parody, UpdateData.link(this));
	}

	@Override
	public void removeContent(Content content) throws DataBaseException
	{
		ref.removeFromContents(
				(org.dyndns.doujindb.db.cayenne.Content)
				((org.dyndns.doujindb.db.ContentImpl)content).ref
			);
		DataBase.fireRecordUpdated(this, UpdateData.unlink(content));
		DataBase.fireRecordUpdated(content, UpdateData.unlink(this));
	}

	@Override
	public void removeParody(Parody parody) throws DataBaseException
	{
		ref.removeFromParodies(
				(org.dyndns.doujindb.db.cayenne.Parody)
				((org.dyndns.doujindb.db.ParodyImpl)parody).ref
			);
		DataBase.fireRecordUpdated(this, UpdateData.unlink(parody));
		DataBase.fireRecordUpdated(parody, UpdateData.unlink(this));
	}
	
	@Override
	public void addAlias(String alias) throws DataBaseException
	{
		if(getAliases().contains(alias))
			return;
		org.dyndns.doujindb.db.cayenne.BookAlias object = DataBase.newBookAlias();
		object.setName(alias);
		ref.addToAliases(object);
	}

	@Override
	public void removeAlias(String alias) throws DataBaseException
	{
		Set<org.dyndns.doujindb.db.cayenne.BookAlias> set = ref.getAliases();
		synchronized(set)
		{
			Iterator<org.dyndns.doujindb.db.cayenne.BookAlias> i = set.iterator();
			while(i.hasNext())
			{
				org.dyndns.doujindb.db.cayenne.BookAlias a = i.next();
				if(a.getName().equals(alias))
				{
					i.remove();
					ref.removeFromAliases(a);
					DataBase.deleteObject(a);
				}
			}
		}
	}
	
	@Override
	public synchronized Integer getId() throws DataBaseException
	{
		return ref.getID();
	}
	
	@Override
	public void doRecycle() throws DataBaseException
	{
		ref.setRecycled(true);
		DataBase.fireRecordRecycled(this);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		ref.setRecycled(false);
		DataBase.fireRecordRestored(this);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ref.getRecycled();
	}

	@Override
	public void removeAll() throws DataBaseException
	{
		for(Artist artist : getArtists())
			removeArtist(artist);
		for(Circle circle : getCircles())
			removeCircle(circle);
		for(Content content : getContents())
			removeContent(content);
		for(Parody parody : getParodies())
			removeParody(parody);
		if(getConvention() != null)
		{
			DataBase.fireRecordUpdated(this, UpdateData.unlink(getConvention()));
			DataBase.fireRecordUpdated(getConvention(), UpdateData.unlink(this));
		}
		setConvention(null);
	}

	@Override
	public int compareTo(Book o) {
		return this.getId().compareTo(o.getId());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Book))
			return false;
		else
			return compareTo((Book)obj) == 0;
	}

	@Override
	protected CayenneDataObject getRef() {
		return ref;
	}
}
