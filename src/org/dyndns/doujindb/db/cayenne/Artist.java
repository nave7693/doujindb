package org.dyndns.doujindb.db.cayenne;

import java.util.Set;

import org.dyndns.doujindb.db.cayenne.auto._Artist;

@SuppressWarnings("serial")
public class Artist extends _Artist
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
}
