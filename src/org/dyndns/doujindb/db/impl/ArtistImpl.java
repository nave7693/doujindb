package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.*;

final class ArtistImpl extends RecordImpl implements Artist, Serializable//, Comparable<Artist>
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
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("japanese_name"));
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
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("translated_name"));
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
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("romaji_name"));
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
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.property("weblink"));
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
		return this.getJapaneseName();
		/*return japaneseName + 
			(romajiName.equals("") ? "" : " ("+romajiName+")") +
			(translatedName.equals("") ? "" : " ("+translatedName+")");*/
	}

	@Override
	public void addBook(Book book) throws DataBaseException
	{
		if(getBooks().contains(book))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).addToBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.link(book));
		((DataBaseImpl)Core.Database)._recordUpdated(book, UpdateData.link(this));
	}

	@Override
	public void addCircle(Circle circle) throws DataBaseException
	{
		if(getCircles().contains(circle))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).addToCircles(
			(org.dyndns.doujindb.db.cayenne.Circle)
			((org.dyndns.doujindb.db.impl.CircleImpl)circle).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.link(circle));
		((DataBaseImpl)Core.Database)._recordUpdated(circle, UpdateData.link(this));
	}

	@Override
	public void removeBook(Book book) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).removeFromBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(book));
		((DataBaseImpl)Core.Database)._recordUpdated(book, UpdateData.unlink(this));
	}

	@Override
	public void removeCircle(Circle circle) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).removeFromCircles(
			(org.dyndns.doujindb.db.cayenne.Circle)
			((org.dyndns.doujindb.db.impl.CircleImpl)circle).ref
		);
		((DataBaseImpl)Core.Database)._recordUpdated(this, UpdateData.unlink(circle));
		((DataBaseImpl)Core.Database)._recordUpdated(circle, UpdateData.unlink(this));
	}

	@Override
	public synchronized String getID() throws DataBaseException
	{
		Integer ID = ((org.dyndns.doujindb.db.cayenne.Artist)ref).getID();
		if(ID == null)
			return null;
		else
			return "A" + String.format("%08x", ID);
	}

	@Override
	public void doRecycle() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setRecycled(true);
		((DataBaseImpl)Core.Database)._recordRecycled(this);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setRecycled(false);
		((DataBaseImpl)Core.Database)._recordRestored(this);
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
}
