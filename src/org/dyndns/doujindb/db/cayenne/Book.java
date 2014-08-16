package org.dyndns.doujindb.db.cayenne;

import java.util.Date;
import java.util.Set;

import org.dyndns.doujindb.db.cayenne.auto._Book;
import org.dyndns.doujindb.db.record.Book.Rating;
import org.dyndns.doujindb.db.record.Book.Type;

@SuppressWarnings("serial")
public class Book extends _Book
{
	@Override
	protected void postAdd() {
		super.setJapaneseName("");
		super.setTranslatedName("");
		super.setRomajiName("");
		super.setInfo("");
		super.setRating(Rating.UNRATED);
		super.setType(Type.不詳);
		super.setAdult(true);
		super.setPages(0);
		super.setPublished(new Date());
		super.setRecycled(false);
	}
	
	public Integer getID() {
		return (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN);
	}

	@Override
	public void addToArtists(Artist artist) {
		if(super.getArtists().contains(artist))
			return;
		super.addToArtists(artist);
	}
	
	public void addToArtists(Iterable<Artist> artist) {
		Set<Artist> artists = super.getArtists();
		for(Artist a : artist)
			if(!artists.contains(a))
				super.addToArtists(a);
	}

	@Override
	public void removeFromArtists(Artist artist) {
		if(!super.getArtists().contains(artist))
			return;
		super.removeFromArtists(artist);
	}
	
	public void removeFromArtists(Iterable<Artist> artist) {
		Set<Artist> artists = super.getArtists();
		for(Artist a : artist)
			if(artists.contains(a))
				super.removeFromArtists(a);
	}

	@Override
	public void addToCircles(Circle circle) {
		if(super.getCircles().contains(circle))
			return;
		super.addToCircles(circle);
	}
	
	public void addToCircles(Iterable<Circle> circle) {
		Set<Circle> circles = super.getCircles();
		for(Circle c : circle)
			if(!circles.contains(c))
				super.addToCircles(c);
	}

	@Override
	public void removeFromCircles(Circle circle) {
		if(!super.getCircles().contains(circle))
			return;
		super.removeFromCircles(circle);
	}
	
	public void removeFromCircles(Iterable<Circle> circle) {
		Set<Circle> circles = super.getCircles();
		for(Circle c : circle)
			if(circles.contains(c))
				super.removeFromCircles(c);
	}

	@Override
	public void addToContents(Content content) {
		if(super.getContents().contains(content))
			return;
		super.addToContents(content);
	}
	
	public void addToContents(Iterable<Content> content) {
		Set<Content> contents = super.getContents();
		for(Content c : content)
			if(!contents.contains(c))
				super.addToContents(c);
	}

	@Override
	public void removeFromContents(Content content) {
		if(!super.getContents().contains(content))
			return;
		super.removeFromContents(content);
	}
	
	public void removeFromContents(Iterable<Content> content) {
		Set<Content> contents = super.getContents();
		for(Content c : content)
			if(contents.contains(c))
				super.removeFromContents(c);
	}

	@Override
	public void addToParodies(Parody parody) {
		if(super.getParodies().contains(parody))
			return;
		super.addToParodies(parody);
	}
	
	public void addToParodies(Iterable<Parody> parody) {
		Set<Parody> parodies = super.getParodies();
		for(Parody p : parody)
			if(!parodies.contains(p))
				super.addToParodies(p);
	}

	@Override
	public void removeFromParodies(Parody parody) {
		if(!super.getParodies().contains(parody))
			return;
		super.removeFromParodies(parody);
	}
	
	public void removeFromParodies(Iterable<Parody> parody) {
		Set<Parody> parodies = super.getParodies();
		for(Parody p : parody)
			if(parodies.contains(p))
				super.removeFromParodies(p);
	}
}
