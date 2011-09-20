package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

final class ArtistImpl extends RecordImpl implements Artist, Serializable//, Comparable<Artist>
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	public ArtistImpl(org.dyndns.doujindb.db.cayenne.Artist ref) throws DataBaseException
	{
		if(ref == null)
			throw new IllegalArgumentException("CayenneDataObject reference can't be null.");
		this.ref = ref;
		doRestore();
	}

	@Override
	public synchronized String getJapaneseName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setJapaneseName(japaneseName);
	}

	@Override
	public synchronized String getTranslatedName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getTranslatedName();
	}

	@Override
	public synchronized void setTranslatedName(String translatedName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setTranslatedName(translatedName);
	}

	@Override
	public synchronized String getRomanjiName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getRomanjiName();
	}

	@Override
	public synchronized void setRomanjiName(String romanjiName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setRomanjiName(romanjiName);
	}

	@Override
	public synchronized String getWeblink() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getWeblink();
	}

	@Override
	public synchronized void setWeblink(String weblink) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setWeblink(weblink);
	}

	@Override
	public synchronized RecordSet<Book> getBooks() throws DataBaseException
	{
		Set<Book> set = new TreeSet<Book>();
		Set<org.dyndns.doujindb.db.cayenne.Book> result = ((org.dyndns.doujindb.db.cayenne.Artist)ref).getBooks();
		for(org.dyndns.doujindb.db.cayenne.Book r : result)
			if(!r.getRecycled())
				try { set.add(new RemoteBook(new RMIBookImpl(new BookImpl(r)))); } catch (RemoteException re) { }
		return new RecordSetImpl<Book>(set);
	}
	
	@Override
	public synchronized RecordSet<Circle> getCircles() throws DataBaseException
	{
		Set<Circle> set = new TreeSet<Circle>();
		Set<org.dyndns.doujindb.db.cayenne.Circle> result = ((org.dyndns.doujindb.db.cayenne.Artist)ref).getCircles();
		for(org.dyndns.doujindb.db.cayenne.Circle r : result)
			if(!r.getRecycled())
				try { set.add(new RemoteCircle(new RMICircleImpl(new CircleImpl(r)))); } catch (RemoteException re) { }
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
	public void addBook(Book book) throws DataBaseException
	{
		if(getBooks().contains(book))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Book.class,
				ExpressionFactory.inDbExp("ID", ((RemoteBook)book).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Book refBook = (org.dyndns.doujindb.db.cayenne.Book) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Artist)ref).addToBooks(refBook);
	}

	@Override
	public void addCircle(Circle circle) throws DataBaseException
	{
		if(getCircles().contains(circle))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Circle.class,
				ExpressionFactory.inDbExp("ID", ((RemoteCircle)circle).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Circle refCircle = (org.dyndns.doujindb.db.cayenne.Circle) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Artist)ref).addToCircles(refCircle);
	}

	@Override
	public void removeBook(Book book) throws DataBaseException
	{
		if(!getBooks().contains(book))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Book.class,
				ExpressionFactory.inDbExp("ID", ((RemoteBook)book).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Book refBook = (org.dyndns.doujindb.db.cayenne.Book) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Artist)ref).removeFromBooks(refBook);
	}

	@Override
	public void removeCircle(Circle circle) throws DataBaseException
	{
		if(!getCircles().contains(circle))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Circle.class,
				ExpressionFactory.inDbExp("ID", ((RemoteCircle)circle).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Circle refCircle = (org.dyndns.doujindb.db.cayenne.Circle) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Artist)ref).removeFromCircles(refCircle);
	}

	@Override
	public synchronized String getID() throws DataBaseException
	{
		//return "A" + super.getID();
		return "A" + ((org.dyndns.doujindb.db.cayenne.Artist)ref).getID();
	}

	@Override
	public void doRecycle() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setRecycled(true);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Artist)ref).setRecycled(false);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Artist)ref).getRecycled();
	}
}
