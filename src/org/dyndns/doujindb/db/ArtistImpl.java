package org.dyndns.doujindb.db;

import java.util.*;

import org.apache.cayenne.CayenneDataObject;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.record.*;

@SuppressWarnings("serial")
final class ArtistImpl extends RecordImpl implements Artist
{
	org.dyndns.doujindb.db.cayenne.Artist ref;
	
	public ArtistImpl(org.dyndns.doujindb.db.cayenne.Artist ref) throws DataBaseException
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
	public synchronized String getWeblink() throws DataBaseException
	{
		return ref.getWeblink();
	}

	@Override
	public synchronized void setWeblink(String weblink) throws DataBaseException
	{
		if(getWeblink().equals(weblink))
			return;
		ref.setWeblink(weblink);
		DataBase.fireRecordUpdated(this, UpdateData.property("weblink"));
	}

	@Override
	public synchronized RecordSet<Book> getBooks() throws DataBaseException
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ref.getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
				set.add(new BookImpl(r));
		return new RecordSetImpl<Book>(set);
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
	public synchronized Set<String> getAliases() throws DataBaseException
	{
		Set<String> set = new TreeSet<String>();
		Set<org.dyndns.doujindb.db.cayenne.ArtistAlias> result = ref.getAliases();
		for(org.dyndns.doujindb.db.cayenne.ArtistAlias r : result)
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
	public void addBook(Book book) throws DataBaseException
	{
		ref.addToBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.BookImpl)book).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.link(book));
		DataBase.fireRecordUpdated(book, UpdateData.link(this));
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
	public void removeBook(Book book) throws DataBaseException
	{
		ref.removeFromBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.BookImpl)book).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.unlink(book));
		DataBase.fireRecordUpdated(book, UpdateData.unlink(this));
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
	public void addAlias(String alias) throws DataBaseException
	{
		if(getAliases().contains(alias))
			return;
		org.dyndns.doujindb.db.cayenne.ArtistAlias object = DataBase.newArtistAlias();
		object.setName(alias);
		ref.addToAliases(object);
	}

	@Override
	public void removeAlias(String alias) throws DataBaseException
	{
		Set<org.dyndns.doujindb.db.cayenne.ArtistAlias> set = ref.getAliases();
		synchronized(set)
		{
			Iterator<org.dyndns.doujindb.db.cayenne.ArtistAlias> i = set.iterator();
			while(i.hasNext())
			{
				org.dyndns.doujindb.db.cayenne.ArtistAlias a = i.next();
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
		for(Book book : getBooks())
			removeBook(book);
		for(Circle circle : getCircles())
			removeCircle(circle);
	}

	@Override
	public int compareTo(Artist o) {
		return this.getId().compareTo(o.getId());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Artist))
			return false;
		else
			return compareTo((Artist)obj) == 0;
	}

	@Override
	protected CayenneDataObject getRef() {
		return ref;
	}
}
