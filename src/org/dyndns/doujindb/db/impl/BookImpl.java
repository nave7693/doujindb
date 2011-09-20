package org.dyndns.doujindb.db.impl;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

final class BookImpl extends RecordImpl implements Book, Serializable//, Comparable<Book>
{
	private static final long serialVersionUID = 0xFEED0001L;
	
	public BookImpl(org.dyndns.doujindb.db.cayenne.Book ref) throws DataBaseException
	{
		if(ref == null)
			throw new IllegalArgumentException("CayenneDataObject reference can't be null.");
		this.ref = ref;
		setRating(Rating.UNRATED);
		setType(Type.同人誌);
		setAdult(true);
		setColored(false);
		setDecensored(false);
		setTranslated(false);
		setPages(0);
		setDate(new Date());
		doRestore();
	}

	@Override
	public synchronized String getJapaneseName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getJapaneseName();
	}

	@Override
	public synchronized void setJapaneseName(String japaneseName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setJapaneseName(japaneseName);
	}

	@Override
	public synchronized String getTranslatedName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getTranslatedName();
	}
	
	@Override
	public synchronized void setTranslatedName(String translatedName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setTranslatedName(translatedName);
	}

	@Override
	public synchronized String getRomanjiName() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getRomanjiName();
	}

	@Override
	public synchronized void setRomanjiName(String romanjiName) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRomanjiName(romanjiName);
	}

	@Override
	public synchronized RecordSet<Artist> getArtists() throws DataBaseException
	{
		Set<Artist> set = new TreeSet<Artist>();
		Set<org.dyndns.doujindb.db.cayenne.Artist> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getArtists();
		for(org.dyndns.doujindb.db.cayenne.Artist r : result)
			if(!r.getRecycled())
				try { set.add(new RemoteArtist(new RMIArtistImpl(new ArtistImpl(r)))); } catch (RemoteException re) { }
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
			if(!a.getRecycled())
				for(org.dyndns.doujindb.db.cayenne.Circle c : a.getCircles())
					if(!c.getRecycled())
						result.add(c);
		for(org.dyndns.doujindb.db.cayenne.Circle r : result)
			try { set.add(new RemoteCircle(new RMICircleImpl(new CircleImpl(r)))); } catch (RemoteException re) { }
		return new RecordSetImpl<Circle>(set);
	}

	@Override
	public synchronized RecordSet<Parody> getParodies() throws DataBaseException
	{
		Set<Parody> set = new TreeSet<Parody>();
		Set<org.dyndns.doujindb.db.cayenne.Parody> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getParodies();
		for(org.dyndns.doujindb.db.cayenne.Parody r : result)
			if(!r.getRecycled())
				try { set.add(new RemoteParody(new RMIParodyImpl(new ParodyImpl(r)))); } catch (RemoteException re) { }
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
		((org.dyndns.doujindb.db.cayenne.Book)ref).setPublished(released);
	}

	@Override
	public synchronized Type getType() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getType();
	}

	@Override
	public synchronized void setType(Type type) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setType(type);
	}

	@Override
	public synchronized boolean isAdult() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getAdult();
	}

	@Override
	public synchronized void setAdult(boolean adult) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setAdult(adult);
	}

	@Override
	public synchronized boolean isDecensored() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getDecensored();
	}

	@Override
	public synchronized void setDecensored(boolean decensored) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setDecensored(decensored);
	}

	@Override
	public synchronized boolean isTranslated() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getTranslated();
	}

	@Override
	public synchronized void setTranslated(boolean translated) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setTranslated(translated);
	}
	
	@Override
	public synchronized boolean isColored() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getColor();
	}

	@Override
	public synchronized void setColored(boolean colored) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setColor(colored);
	}

	@Override
	public synchronized Rating getRating() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getRating();
	}

	@Override
	public synchronized void setRating(Rating rating) throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRating(rating);
	}
	
	@Override
	public synchronized RecordSet<Content> getContents() throws DataBaseException
	{
		Set<Content> set = new TreeSet<Content>();
		Set<org.dyndns.doujindb.db.cayenne.Content> result = ((org.dyndns.doujindb.db.cayenne.Book)ref).getContents();
		for(org.dyndns.doujindb.db.cayenne.Content r : result)
			if(!r.getRecycled())
				try { set.add(new RemoteContent(new RMIContentImpl(new ContentImpl(r)))); } catch (RemoteException re) { }
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
		((org.dyndns.doujindb.db.cayenne.Book)ref).setPages(pages);
	}

	@Override
	public synchronized Convention getConvention() throws DataBaseException
	{
		if(((org.dyndns.doujindb.db.cayenne.Book)ref).getConventionof() == null)
			return null;
		try { return new RemoteConvention(new RMIConventionImpl(new ConventionImpl(((org.dyndns.doujindb.db.cayenne.Book)ref).getConventionof()))); } catch (RemoteException re) { return null; }
	}

	@Override
	public synchronized void setConvention(Convention convention) throws DataBaseException
	{
		if(convention == null)
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Convention.class,
				ExpressionFactory.inDbExp("ID", ((RemoteConvention)convention).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Convention refConvention = (org.dyndns.doujindb.db.cayenne.Convention) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Book)ref).setConventionof(refConvention);
	}

	@Override
	public synchronized String getInfo() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getInfo();
	}

	@Override
	public synchronized void setInfo(String info) throws DataBaseException
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
	public void addArtist(Artist artist) throws DataBaseException
	{
		if(getArtists().contains(artist))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Artist.class,
				ExpressionFactory.inDbExp("ID", ((RemoteArtist)artist).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Artist refArtist = (org.dyndns.doujindb.db.cayenne.Artist) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Book)ref).addToArtists(refArtist);
	}

	@Override
	public void removeArtist(Artist artist) throws DataBaseException
	{
		if(!getArtists().contains(artist))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Artist.class,
				ExpressionFactory.inDbExp("ID", ((RemoteArtist)artist).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Artist refArtist = (org.dyndns.doujindb.db.cayenne.Artist) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromArtists(refArtist);
	}

	@Override
	public void addContent(Content content) throws DataBaseException
	{
		if(getContents().contains(content))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Content.class,
				ExpressionFactory.inDbExp("ID", ((RemoteContent)content).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Content refContent = (org.dyndns.doujindb.db.cayenne.Content) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Book)ref).addToContents(refContent);
	}

	@Override
	public void addParody(Parody parody) throws DataBaseException
	{
		if(getParodies().contains(parody))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Parody.class,
				ExpressionFactory.inDbExp("ID", ((RemoteParody)parody).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Parody refParody = (org.dyndns.doujindb.db.cayenne.Parody) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Book)ref).addToParodies(refParody);
	}

	@Override
	public void removeContent(Content content) throws DataBaseException
	{
		if(!getContents().contains(content))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Content.class,
				ExpressionFactory.inDbExp("ID", ((RemoteContent)content).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Content refContent = (org.dyndns.doujindb.db.cayenne.Content) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromContents(refContent);
	}

	@Override
	public void removeParody(Parody parody) throws DataBaseException
	{
		if(!getParodies().contains(parody))
			return;
		SelectQuery select = new SelectQuery(
				org.dyndns.doujindb.db.cayenne.Parody.class,
				ExpressionFactory.inDbExp("ID", ((RemoteParody)parody).getID().substring(1)));
		org.dyndns.doujindb.db.cayenne.Parody refParody = (org.dyndns.doujindb.db.cayenne.Parody) DataBaseImpl.context.performQuery(select).get(0);
		((org.dyndns.doujindb.db.cayenne.Book)ref).removeFromParodies(refParody);
	}
	
	@Override
	public synchronized String getID() throws DataBaseException
	{
		//return "B" + super.getID();
		return "B" + ((org.dyndns.doujindb.db.cayenne.Book)ref).getID();
	}
	
	@Override
	public void doRecycle() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRecycled(true);
	}

	@Override
	public void doRestore() throws DataBaseException
	{
		((org.dyndns.doujindb.db.cayenne.Book)ref).setRecycled(false);
	}

	@Override
	public boolean isRecycled() throws DataBaseException
	{
		return ((org.dyndns.doujindb.db.cayenne.Book)ref).getRecycled();
	}
}
