package org.dyndns.doujindb.db;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.*;

final class ArtistImpl extends RecordImpl implements Artist, Serializable
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	public ArtistImpl(org.dyndns.doujindb.db.cayenne.Artist ref) throws DataBaseException
	{
		this.ref = ref;
	}

	@Override
	public synchronized String getJapaneseName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName) throws DataBaseException
	{
		if(getJapaneseName().equals(japaneseName))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setJapaneseName(japaneseName);
		DataBase.fireRecordUpdated(this, UpdateData.property("japanese_name"));
	}

	@Override
	public synchronized String getTranslatedName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getTranslatedName();
	}

	@Override
	public synchronized void setTranslatedName(String translatedName) throws DataBaseException
	{
		if(getTranslatedName().equals(translatedName))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setTranslatedName(translatedName);
		DataBase.fireRecordUpdated(this, UpdateData.property("translated_name"));
	}

	@Override
	public synchronized String getRomajiName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getRomajiName();
	}

	@Override
	public synchronized void setRomajiName(String romajiName) throws DataBaseException
	{
		if(getRomajiName().equals(romajiName))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setRomajiName(romajiName);
		DataBase.fireRecordUpdated(this, UpdateData.property("romaji_name"));
	}

	@Override
	public synchronized String getWeblink() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getWeblink();
	}

	@Override
	public synchronized void setWeblink(String weblink) throws DataBaseException
	{
		if(getWeblink().equals(weblink))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setWeblink(weblink);
		DataBase.fireRecordUpdated(this, UpdateData.property("weblink"));
	}

	@Override
	public synchronized RecordSet<Book> getBooks() throws DataBaseException
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ((org.dyndns.doujindb.db.cayenne.Artist)ref).getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
				set.add(new BookImpl(r));
		return new RecordSetImpl<Book>(set);
	}
	
	@Override
	public synchronized RecordSet<Circle> getCircles() throws DataBaseException
	{
		Set<Circle> set = new TreeSet<Circle>();
		Set<org.dyndns.doujindb.db.cayenne.Circle> result = ((org.dyndns.doujindb.db.cayenne.Artist)ref).getCircles();
		for(org.dyndns.doujindb.db.cayenne.Circle r : result)
				set.add(new CircleImpl(r));
		return new RecordSetImpl<Circle>(set);
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
		if(getBooks().contains(book))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).addToBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.BookImpl)book).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.link(book));
		DataBase.fireRecordUpdated(book, UpdateData.link(this));
	}

	@Override
	public void addCircle(Circle circle) throws DataBaseException
	{
		if(getCircles().contains(circle))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).addToCircles(
			(org.dyndns.doujindb.db.cayenne.Circle)
			((org.dyndns.doujindb.db.CircleImpl)circle).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.link(circle));
		DataBase.fireRecordUpdated(circle, UpdateData.link(this));
	}

	@Override
	public void removeBook(Book book) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).removeFromBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.BookImpl)book).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.unlink(book));
		DataBase.fireRecordUpdated(book, UpdateData.unlink(this));
	}

	@Override
	public void removeCircle(Circle circle) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).removeFromCircles(
			(org.dyndns.doujindb.db.cayenne.Circle)
			((org.dyndns.doujindb.db.CircleImpl)circle).ref
		);
		DataBase.fireRecordUpdated(this, UpdateData.unlink(circle));
		DataBase.fireRecordUpdated(circle, UpdateData.unlink(this));
	}

	@Override
	public synchronized Integer getId() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getID();
	}

	@Override
	public void doRecycle() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setRecycled(true);
		DataBase.fireRecordRecycled(this);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setRecycled(false);
		DataBase.fireRecordRestored(this);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getRecycled();
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
}
