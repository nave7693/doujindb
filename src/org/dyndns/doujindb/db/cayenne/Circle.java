package org.dyndns.doujindb.db.cayenne;

import java.util.Set;

import org.dyndns.doujindb.db.cayenne.auto._Circle;

@SuppressWarnings("serial")
public class Circle extends _Circle
{
	@Override
	protected void postAdd() {
		super.setJapaneseName("");
		super.setTranslatedName("");
		super.setRomajiName("");
		super.setWeblink("");
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
	public void addToBooks(Book book) {
		if(super.getBooks().contains(book))
			return;
		super.addToBooks(book);
	}
	
	public void addToBooks(Iterable<Book> book) {
		Set<Book> books = super.getBooks();
		for(Book b : book)
			if(!books.contains(b))
				super.addToBooks(b);
	}

	@Override
	public void removeFromBooks(Book book) {
		if(!super.getBooks().contains(book))
			return;
		super.removeFromBooks(book);
	}
	
	public void removeFromBooks(Iterable<Book> book) {
		Set<Book> books = super.getBooks();
		for(Book b : book)
			if(books.contains(b))
				super.removeFromBooks(b);
	}
}
