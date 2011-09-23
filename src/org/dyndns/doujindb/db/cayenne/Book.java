package org.dyndns.doujindb.db.cayenne;

import java.util.Date;

import org.apache.cayenne.PersistenceState;
import org.dyndns.doujindb.db.cayenne.auto._Book;
import org.dyndns.doujindb.db.records.Book.Rating;
import org.dyndns.doujindb.db.records.Book.Type;

@SuppressWarnings("serial")
public class Book extends _Book {
	public void setPersistenceState(int state)
	{
		super.setPersistenceState(state);
		if (state == PersistenceState.NEW)
		{
			super.setRating(Rating.UNRATED);
			super.setType(Type.同人誌);
			super.setAdult(true);
			super.setColor(false);
			super.setDecensored(false);
			super.setTranslated(false);
			super.setPages(0);
			super.setPublished(new Date());
			super.setRecycled(false);
		}
	}
}
