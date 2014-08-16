package org.dyndns.doujindb.db.cayenne;

import java.util.Set;

import org.dyndns.doujindb.db.cayenne.auto._Parody;

@SuppressWarnings("serial")
public class Parody extends _Parody
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
}
