package org.dyndns.doujindb.db.cayenne;

import java.util.Date;

import org.dyndns.doujindb.db.cayenne.auto._Book;
import org.dyndns.doujindb.db.records.Book.Rating;
import org.dyndns.doujindb.db.records.Book.Type;

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
		super.setColor(false);
		super.setDecensored(false);
		super.setTranslated(false);
		super.setPages(0);
		super.setPublished(new Date());
		super.setRecycled(false);
	}
	
	public Integer getID() {
		return (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN);
	}
}
