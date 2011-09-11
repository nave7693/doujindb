package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.util.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace = "org.dyndns.doujindb.core.db.dbo", name="Artist")
final class ArtistImpl extends RecordImpl implements Artist, Serializable//, Comparable<Artist>
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	public ArtistImpl(org.dyndns.doujindb.db.cayenne.Artist ref)
	{
		this.ref = ref;
	}

	@Override
	public synchronized String getJapaneseName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName)
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setJapaneseName(japaneseName);
	}

	@Override
	public synchronized String getTranslatedName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getTranslatedName();
	}

	@Override
	public synchronized void setTranslatedName(String translatedName)
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setTranslatedName(translatedName);
	}

	@Override
	public synchronized String getRomanjiName()
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getRomanjiName();
	}

	@Override
	public synchronized void setRomanjiName(String romanjiName)
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setRomanjiName(romanjiName);
	}

	@Override
	public synchronized String getWeblink()
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getWeblink();
	}

	@Override
	public synchronized void setWeblink(String weblink)
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setWeblink(weblink);
	}

	@Override
	public synchronized RecordSet<Book> getBooks()
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ((org.dyndns.doujindb.db.cayenne.Artist)ref).getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
			set.add(new BookImpl(r));
		return new RecordSetImpl<Book>(set);
	}
	
	@Override
	public synchronized RecordSet<Circle> getCircles()
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
			(romanjiName.equals("") ? "" : " ("+romanjiName+")") +
			(translatedName.equals("") ? "" : " ("+translatedName+")");*/
	}

	@Override
	public void addBook(Book book) {
		if(getBooks().contains(book))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).addToBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
	}

	@Override
	public void addCircle(Circle circle) {
		if(getCircles().contains(circle))
			return;
		((org.dyndns.doujindb.db.cayenne.Artist)ref).addToCircles(
			(org.dyndns.doujindb.db.cayenne.Circle)
			((org.dyndns.doujindb.db.impl.CircleImpl)circle).ref
		);
	}

	@Override
	public void removeBook(Book book) {
		((org.dyndns.doujindb.db.cayenne.Artist)ref).removeFromBooks(
			(org.dyndns.doujindb.db.cayenne.Book)
			((org.dyndns.doujindb.db.impl.BookImpl)book).ref
		);
	}

	@Override
	public void removeCircle(Circle circle) {
		((org.dyndns.doujindb.db.cayenne.Artist)ref).removeFromCircles(
			(org.dyndns.doujindb.db.cayenne.Circle)
			((org.dyndns.doujindb.db.impl.CircleImpl)circle).ref
		);
	}
}
